package com.laurel.actiontracker;

import com.laurel.actiontracker.controller.UserController;
import com.laurel.actiontracker.dto.request.UpdateUserRoleRequest;
import com.laurel.actiontracker.dto.response.UserResponse;
import com.laurel.actiontracker.entity.User;
import com.laurel.actiontracker.exception.ResourceNotFoundException;
import com.laurel.actiontracker.security.CustomUserDetailsService;
import com.laurel.actiontracker.security.JwtUtil;
import com.laurel.actiontracker.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import static com.laurel.actiontracker.entity.User.Role.ADMIN;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(UserControllerTest.MethodSecurityTestConfig.class)
public class UserControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_returns200ForAdmin() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setFullName("Full Name");
        user.setEmail("testuser@mail.com");
        user.setRole(ADMIN);

        when(userService.getAllUsers(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(UserResponse.from(user))));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].email").value("testuser@mail.com"));
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void getAllUsers_returns403ForNonAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_returns200ForAdmin() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setFullName("Full Name");
        user.setEmail("testuser@mail.com");
        user.setRole(ADMIN);

        when(userService.getUserById(1L)).thenReturn(UserResponse.from(user));

        mockMvc.perform(get("/api/v1/users/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("testuser@mail.com"));
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void getUserById_returns403ForNonAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}", 1L))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_returns404WhenUserNotFound() throws Exception {
        when(userService.getUserById(1L)).thenThrow(new ResourceNotFoundException("User not found with id: 1"));

        mockMvc.perform(get("/api/v1/users/{id}", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserRole_returns200ForAdmin() throws Exception{
        UpdateUserRoleRequest request = UpdateUserRoleRequest.builder()
                .role(ADMIN)
                .build();

        User user = new User();
        user.setId(1L);
        user.setFullName("Full Name");
        user.setEmail("testuser@mail.com");
        user.setRole(ADMIN);

        when(userService.updateUserRole(eq(1L), any(UpdateUserRoleRequest.class))).thenReturn(UserResponse.from(user));

        mockMvc.perform(put("/api/v1/users/{id}/role", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void updateUserRole_returns403ForNonAdmin() throws  Exception{
        UpdateUserRoleRequest request = UpdateUserRoleRequest.builder()
                .role(ADMIN)
                .build();

        mockMvc.perform(put("/api/v1/users/{id}/role", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserRole_returns404WhenUserNotFound() throws Exception {
        UpdateUserRoleRequest request = UpdateUserRoleRequest.builder()
                .role(ADMIN)
                .build();

        when(userService.updateUserRole(eq(1L), any(UpdateUserRoleRequest.class))).thenThrow(new ResourceNotFoundException("User not found with id : 1L"));

        mockMvc.perform(put("/api/v1/users/{id}/role", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        }
}
