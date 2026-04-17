package com.college.eventclub.dto;

import com.college.eventclub.model.Role;

public class RegisterResponse {

    private Long userId;
    private String fullName;
    private String email;
    private Role role;

    public RegisterResponse(Long userId, String fullName, String email, Role role) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }

    public Long getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }
}
