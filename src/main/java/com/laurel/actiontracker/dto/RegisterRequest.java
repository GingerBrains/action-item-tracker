package com.laurel.actiontracker.dto;

import lombok.Getter;

@Getter
public class RegisterRequest {
    private String email;
    private String password;
    private String fullName;

}