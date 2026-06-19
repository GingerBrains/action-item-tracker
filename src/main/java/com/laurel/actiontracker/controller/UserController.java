package com.laurel.actiontracker.controller;

import com.laurel.actiontracker.dto.request.UpdateUserRoleRequest;
import com.laurel.actiontracker.dto.response.UserResponse;
import com.laurel.actiontracker.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
     private final UserService userService;


    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUserRole(@PathVariable Long id, @Valid @RequestBody UpdateUserRoleRequest request){
        return ResponseEntity.ok(userService.updateUserRole(id, request));
    }
}
