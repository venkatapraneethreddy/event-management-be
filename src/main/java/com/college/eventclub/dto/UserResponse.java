package com.college.eventclub.dto;

import com.college.eventclub.model.Role;
import com.college.eventclub.model.User;

import java.time.LocalDateTime; // ✅ add this

public class UserResponse {

    private Long userId;
    private String fullName;
    private String email;
    private Role role;

    // ✅ ADD THIS FIELD
    private LocalDateTime createdAt;

    public UserResponse(User user) {
        this.userId = user.getUserId();
        this.fullName = user.getFullName();
        this.email = user.getEmail();
        this.role = user.getRole();

        // ✅ MAP IT FROM ENTITY
        this.createdAt = user.getCreatedAt();
    }

    public Long getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }

    // ✅ ADD GETTER
    public LocalDateTime getCreatedAt() { return createdAt; }
}