package com.laurel.actiontracker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.laurel.actiontracker.controller.AuthController;
import com.laurel.actiontracker.dto.LoginRequest;
import com.laurel.actiontracker.dto.RegisterRequest;
import com.laurel.actiontracker.repository.UserRepository;
import com.laurel.actiontracker.security.CustomUserDetailsService;
import com.laurel.actiontracker.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {

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
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Test
    void register_return200() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("fakemail.company.com")
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
                .email("fakemail.company.com")
                .password("someStrongPassword")
                .build();

        UserDetails userDetails = User.builder()
                .username("fakemail.company.com")
                .password("hashedPassword")
                .roles("MEMBER")
                .build();

        when(authenticationManager.authenticate(any())).thenReturn(new UsernamePasswordAuthenticationToken("fakemail.company.com", "someStrongPassword"));
        when(customUserDetailsService.loadUserByUsername("fakemail.company.com")).thenReturn(userDetails);
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
                .email("fakemail.company.com")
                .password("wrongPassword")
                .build();

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid email or password"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

}
