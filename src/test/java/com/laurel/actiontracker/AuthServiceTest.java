package com.laurel.actiontracker;

import com.laurel.actiontracker.dto.internal.RefreshTokenResult;
import com.laurel.actiontracker.dto.request.LoginRequest;
import com.laurel.actiontracker.dto.request.RegisterRequest;
import com.laurel.actiontracker.dto.response.AuthResponse;
import com.laurel.actiontracker.entity.RefreshToken;
import com.laurel.actiontracker.entity.User;
import com.laurel.actiontracker.exception.EmailAlreadyExistsException;
import com.laurel.actiontracker.exception.ResourceNotFoundException;
import com.laurel.actiontracker.exception.TokenExpiredException;
import com.laurel.actiontracker.repository.UserRepository;
import com.laurel.actiontracker.security.JwtUtil;
import com.laurel.actiontracker.service.RefreshTokenService;
import com.laurel.actiontracker.service.impl.AuthServiceImpl;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserDetailsService userDetailsService;
    @Mock private JwtUtil jwtUtil;
    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private PasswordEncoder passwordEncoder;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(
                authenticationManager,
                userDetailsService,
                jwtUtil,
                userRepository,
                refreshTokenService,
                passwordEncoder,
                604800000L
        );
    }

    // --- login ---

    @Test
    void login_returnsAccessTokenAndSetsRefreshCookie() {
        LoginRequest request = LoginRequest.builder()
                .email("user@mail.com")
                .password("password")
                .build();

        User user = new User();
        user.setEmail("user@mail.com");

        UserDetails userDetails = mock(UserDetails.class);
        RefreshToken refreshToken = new RefreshToken("hashedToken", user, Instant.now().plusSeconds(3600));
        RefreshTokenResult tokenResult = new RefreshTokenResult("rawToken", refreshToken);

        when(userDetailsService.loadUserByUsername("user@mail.com")).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("accessToken");
        when(userRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(user));
        when(refreshTokenService.createRefreshToken(user)).thenReturn(tokenResult);

        HttpServletResponse response = mock(HttpServletResponse.class);

        AuthResponse result = authService.login(request, response);

        assertThat(result.getAccessToken()).isEqualTo("accessToken");
        verify(response, times(1)).addCookie(any(Cookie.class));
    }

    @Test
    void login_throwsBadCredentialsWhenAuthFails() {
        LoginRequest request = LoginRequest.builder()
                .email("user@mail.com")
                .password("wrongPassword")
                .build();

        doThrow(new BadCredentialsException("Invalid email or password"))
                .when(authenticationManager).authenticate(any());

        assertThatThrownBy(() -> authService.login(request, mock(HttpServletResponse.class)))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void login_throwsResourceNotFoundWhenUserNotInRepo() {
        LoginRequest request = LoginRequest.builder()
                .email("user@mail.com")
                .password("password")
                .build();

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername("user@mail.com")).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("accessToken");
        when(userRepository.findByEmail("user@mail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request, mock(HttpServletResponse.class)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("user@mail.com");
    }

    // --- refresh ---

    @Test
    void refresh_returnsNewAccessTokenAndRotatesRefreshToken() {
        User user = new User();
        user.setEmail("user@mail.com");

        RefreshToken existingToken = new RefreshToken("hash", user, Instant.now().plusSeconds(3600));
        RefreshTokenResult newTokenResult = new RefreshTokenResult("newRawToken", existingToken);
        UserDetails userDetails = mock(UserDetails.class);

        when(userDetailsService.loadUserByUsername("user@mail.com")).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("newAccessToken");
        when(refreshTokenService.validateAndGetByRawToken("rawToken")).thenReturn(existingToken);
        when(refreshTokenService.createRefreshToken(user)).thenReturn(newTokenResult);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("refreshToken", "rawToken")});
        HttpServletResponse response = mock(HttpServletResponse.class);

        AuthResponse result = authService.refresh(request, response);

        assertThat(result.getAccessToken()).isEqualTo("newAccessToken");

        org.mockito.InOrder inOrder = inOrder(refreshTokenService);
        inOrder.verify(refreshTokenService).deleteToken(existingToken);
        inOrder.verify(refreshTokenService).createRefreshToken(user);

        verify(response, times(1)).addCookie(any(Cookie.class));
    }

    @Test
    void refresh_throwsTokenExpiredWhenNoCookie() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(null);

        assertThatThrownBy(() -> authService.refresh(request, mock(HttpServletResponse.class)))
                .isInstanceOf(TokenExpiredException.class)
                .hasMessageContaining("No refresh token found");
    }

    @Test
    void refresh_throwsWhenTokenExpired() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("refreshToken", "expiredToken")});
        when(refreshTokenService.validateAndGetByRawToken("expiredToken"))
                .thenThrow(new TokenExpiredException("Refresh token has expired"));

        assertThatThrownBy(() -> authService.refresh(request, mock(HttpServletResponse.class)))
                .isInstanceOf(TokenExpiredException.class);
    }

    @Test
    void refresh_throwsWhenTokenNotFound() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("refreshToken", "unknownToken")});
        when(refreshTokenService.validateAndGetByRawToken("unknownToken"))
                .thenThrow(new ResourceNotFoundException("Token not found"));

        assertThatThrownBy(() -> authService.refresh(request, mock(HttpServletResponse.class)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- logout ---

    @Test
    void logout_deletesTokenAndClearsCookie() {
        User user = new User();
        RefreshToken token = new RefreshToken("hash", user, Instant.now().plusSeconds(3600));

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("refreshToken", "rawToken")});
        when(refreshTokenService.validateAndGetByRawToken("rawToken")).thenReturn(token);

        HttpServletResponse response = mock(HttpServletResponse.class);

        authService.logout(request, response);

        verify(refreshTokenService, times(1)).deleteToken(token);
        verify(response, times(1)).addCookie(any(Cookie.class));
    }

    @Test
    void logout_clearsCookieWhenNoCookiePresent() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(null);

        HttpServletResponse response = mock(HttpServletResponse.class);

        authService.logout(request, response);

        verify(refreshTokenService, never()).deleteToken(any());
        verify(response, times(1)).addCookie(any(Cookie.class));
    }

    @Test
    void logout_clearsCookieWhenTokenExpired() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("refreshToken", "expiredToken")});
        when(refreshTokenService.validateAndGetByRawToken("expiredToken"))
                .thenThrow(new TokenExpiredException("Refresh token has expired"));

        HttpServletResponse response = mock(HttpServletResponse.class);

        authService.logout(request, response);

        verify(response, times(1)).addCookie(any(Cookie.class));
    }

    @Test
    void logout_clearsCookieWhenTokenNotFound() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("refreshToken", "unknownToken")});
        when(refreshTokenService.validateAndGetByRawToken("unknownToken"))
                .thenThrow(new ResourceNotFoundException("Token not found"));

        HttpServletResponse response = mock(HttpServletResponse.class);

        authService.logout(request, response);

        verify(response, times(1)).addCookie(any(Cookie.class));
    }

    // --- register ---

    @Test
    void register_savesNewUser() {
        RegisterRequest request = RegisterRequest.builder()
                .email("newuser@mail.com")
                .fullName("New User")
                .password("password123")
                .build();

        when(userRepository.findByEmail("newuser@mail.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");

        authService.register(request);

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_throwsWhenEmailAlreadyExists() {
        RegisterRequest request = RegisterRequest.builder()
                .email("existing@mail.com")
                .fullName("Existing User")
                .password("password123")
                .build();

        when(userRepository.findByEmail("existing@mail.com")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("existing@mail.com");
    }
}
