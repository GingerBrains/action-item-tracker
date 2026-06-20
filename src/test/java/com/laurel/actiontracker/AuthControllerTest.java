package com.laurel.actiontracker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.laurel.actiontracker.controller.AuthController;
import com.laurel.actiontracker.dto.LoginRequest;
import com.laurel.actiontracker.dto.RegisterRequest;
import com.laurel.actiontracker.dto.response.UserResponse;
import com.laurel.actiontracker.exception.ResourceNotFoundException;
import com.laurel.actiontracker.repository.UserRepository;
import com.laurel.actiontracker.security.CustomUserDetailsService;
import com.laurel.actiontracker.security.JwtUtil;
import com.laurel.actiontracker.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.MockMvcBuilderCustomizer;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static com.laurel.actiontracker.entity.User.Role.ADMIN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(com.laurel.actiontracker.security.SecurityConfig.class)
public class AuthControllerTest {

    @TestConfiguration
    static class MockMvcSecurityConfig {
        @Bean
        MockMvcBuilderCustomizer securityConfigurer() {
            return builder -> builder.apply(SecurityMockMvcConfigurers.springSecurity());
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Test
    void register_return200() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("fakemail@company.com")
                .fullName("Fake Name")
                .password("someStrongPassword")
                .build();

        when(passwordEncoder.encode("someStrongPassword")).thenReturn("someHashedValue");
        when(userRepository.save(any())).thenReturn(new com.laurel.actiontracker.entity.User());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void register_returns409WhenEmailAlreadyExists() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("existing@company.com")
                .fullName("Existing User")
                .password("somePassword123")
                .build();

        when(userRepository.findByEmail("existing@company.com"))
                .thenReturn(Optional.of(new com.laurel.actiontracker.entity.User()));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void login_returns200WithToken() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("fakemail@company.com")
                .password("someStrongPassword")
                .build();

        UserDetails userDetails = User.builder()
                .username("fakemail@company.com")
                .password("hashedPassword")
                .roles("MEMBER")
                .build();

        when(authenticationManager.authenticate(any())).thenReturn(new UsernamePasswordAuthenticationToken("fakemail.company.com", "someStrongPassword"));
        when(customUserDetailsService.loadUserByUsername("fakemail@company.com")).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("someToken");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("someToken"));

    }

    @Test
    void login_returns401ForBadCredentials() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("fakemail@company.com")
                .password("wrongPassword")
                .build();

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid email or password"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void register_returns400WhenEmailIsBlank() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("")
                .fullName("Some Name")
                .password("somePassword123")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_returns400WhenEmailIsBlank() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("")
                .password("somePassword123")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser@mail.com")
    void getCurrentUser_returns200WithUserDetails() throws Exception {
        com.laurel.actiontracker.entity.User user = new com.laurel.actiontracker.entity.User();
        user.setId(1L);
        user.setFullName("Full Name");
        user.setEmail("testuser@mail.com");
        user.setRole(ADMIN);

        when(userService.getUserByEmail("testuser@mail.com")).thenReturn(UserResponse.from(user));

        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("testuser@mail.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @WithMockUser(username = "missing@mail.com")
    void getCurrentUser_returns404WhenUserNotFound() throws Exception {
        when(userService.getUserByEmail("missing@mail.com"))
                .thenThrow(new ResourceNotFoundException("User not found with email: missing@mail.com"));

        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isNotFound());
    }

}
