package com.laurel.actiontracker;

import com.laurel.actiontracker.dto.request.UpdateUserRoleRequest;
import com.laurel.actiontracker.dto.response.UserResponse;
import com.laurel.actiontracker.entity.User;
import com.laurel.actiontracker.exception.ResourceNotFoundException;
import com.laurel.actiontracker.repository.UserRepository;
import com.laurel.actiontracker.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserServiceImpl userService;

    @Test
    void updateUserRole_updatesAndReturnsUser() {
        User existing = new User();
        existing.setId(1L);
        existing.setEmail("testuser@mail.com");
        existing.setFullName("User Name");
        existing.setRole(User.Role.MEMBER);

        UpdateUserRoleRequest request = UpdateUserRoleRequest.builder()
                .role(User.Role.ADMIN)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.updateUserRole(1L, request);

        assertThat(response.getRole()).isEqualTo(User.Role.ADMIN);

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUserRole_throwsWhenUserNotFound() {
        UpdateUserRoleRequest request = UpdateUserRoleRequest.builder()
                .role(User.Role.ADMIN)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUserRole(1L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("1");


    }

    @Test
    void getUserByEmail_returnsUser(){
        String email = "testuser@mail.com";
        User user = new User();
        user.setId(1L);
        user.setEmail(email);
        user.setFullName("User Name");
        user.setRole(User.Role.MEMBER);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserByEmail(email);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getFullName()).isEqualTo("User Name");

        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void getUserByEmail_throwsWhenNotFound(){
        String email = "testuser@mail.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByEmail(email))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(email);
    }
}
