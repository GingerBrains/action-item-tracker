package com.laurel.actiontracker.service;

import com.laurel.actiontracker.dto.request.UpdateUserRoleRequest;
import com.laurel.actiontracker.dto.response.UserResponse;

public interface UserService {
    UserResponse updateUserRole(Long userId, UpdateUserRoleRequest request);
    UserResponse getUserByEmail(String email);
}
