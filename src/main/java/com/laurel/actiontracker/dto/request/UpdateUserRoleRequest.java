package com.laurel.actiontracker.dto.request;

import com.laurel.actiontracker.entity.User;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRoleRequest {

    @NotNull(message = "Role is required")
    private User.Role role;
}