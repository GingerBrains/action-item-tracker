package com.laurel.actiontracker.dto.response;

import com.laurel.actiontracker.entity.User;
import lombok.Getter;

@Getter
public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private User.Role role;

    public static UserResponse from(User user){
        UserResponse dto = new UserResponse();
        dto.id = user.getId();
        dto.fullName = user.getFullName();
        dto.email = user.getEmail();
        dto.role = user.getRole();
        return dto;
    }
}
