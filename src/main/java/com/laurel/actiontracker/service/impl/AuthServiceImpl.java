package com.laurel.actiontracker.service.impl;

import com.laurel.actiontracker.dto.internal.RefreshTokenResult;
import com.laurel.actiontracker.dto.request.LoginRequest;
import com.laurel.actiontracker.dto.request.RefreshTokenRequest;
import com.laurel.actiontracker.dto.request.RegisterRequest;
import com.laurel.actiontracker.dto.response.AuthResponse;
import com.laurel.actiontracker.entity.RefreshToken;
import com.laurel.actiontracker.entity.User;
import com.laurel.actiontracker.exception.EmailAlreadyExistsException;
import com.laurel.actiontracker.exception.ResourceNotFoundException;
import com.laurel.actiontracker.exception.TokenExpiredException;
import com.laurel.actiontracker.repository.UserRepository;
import com.laurel.actiontracker.security.JwtUtil;
import com.laurel.actiontracker.service.AuthService;
import com.laurel.actiontracker.service.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final long refreshExpirationMs;
    private final boolean cookieSecure;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           UserDetailsService userDetailsService,
                           JwtUtil jwtUtil,
                           UserRepository userRepository,
                           RefreshTokenService refreshTokenService,
                           PasswordEncoder passwordEncoder,
                           @Value("${app.jwt.refresh-expiration-ms}") long refreshExpirationMs,
                           @Value("${app.cookie.secure:false}") boolean cookieSecure) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.refreshTokenService = refreshTokenService;
        this.passwordEncoder = passwordEncoder;
        this.refreshExpirationMs = refreshExpirationMs;
        this.cookieSecure = cookieSecure;
    }

    @Override
    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String accessToken = jwtUtil.generateToken(userDetails);
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));
        RefreshTokenResult refreshTokenResult = refreshTokenService.createRefreshToken(user);
        addRefreshTokenCookie(response, refreshTokenResult.rawToken());
        return new AuthResponse(accessToken);
    }

    @Override
    public AuthResponse refresh(HttpServletRequest request, HttpServletResponse response) {
        String rawToken = extractRefreshTokenFromCookie(request);
        if (rawToken == null) {
            throw new TokenExpiredException("No refresh token found");
        }

        RefreshToken existingToken = refreshTokenService.validateAndGetByRawToken(rawToken);
        User user = existingToken.getUser();

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtUtil.generateToken(userDetails);

        refreshTokenService.deleteToken(existingToken);
        String newRawToken = refreshTokenService.createRefreshToken(user).rawToken();

        addRefreshTokenCookie(response, newRawToken);

        return new AuthResponse(accessToken);
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            String rawToken = extractRefreshTokenFromCookie(request);
            if (rawToken != null) {
                RefreshToken token = refreshTokenService.validateAndGetByRawToken(rawToken);
                refreshTokenService.deleteToken(token);
            }
        } catch (TokenExpiredException | ResourceNotFoundException e) {
            // token already expired or not found - just clear the cookie
        }
        clearRefreshTokenCookie(response);
    }

    @Override
    public void register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("Email already registered: " + request.getEmail());
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.Role.MEMBER);

        userRepository.save(user);
    }

    private void addRefreshTokenCookie(HttpServletResponse response, String rawToken) {
        Cookie cookie = new Cookie("refreshToken", rawToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/api/v1/auth"); // only sent to auth endpoints
        cookie.setMaxAge((int)(refreshExpirationMs / 1000)); // seconds, not ms
        cookie.setAttribute("SameSite", "Strict");
        response.addCookie(cookie);
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("refreshToken", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/api/v1/auth");
        cookie.setMaxAge(0); // immediately expire
        response.addCookie(cookie);
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> "refreshToken".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
