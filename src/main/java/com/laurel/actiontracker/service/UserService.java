package com.laurel.actiontracker.service;

import com.laurel.actiontracker.dto.request.UpdateUserRoleRequest;
import com.laurel.actiontracker.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    List<UserResponse> getAllUsers();
    UserResponse getUserById(Long id);
    UserResponse updateUserRole(Long userId, UpdateUserRoleRequest request);
    UserResponse getUserByEmail(String email);
}
