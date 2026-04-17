package com.college.eventclub.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.college.eventclub.dto.UserResponse;
import com.college.eventclub.model.User;
import com.college.eventclub.service.UserService;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public ProfileController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    // GET /api/profile — fetch logged-in user's profile
    @GetMapping
    public ResponseEntity<?> getProfile(Authentication authentication) {
        String email = authentication.getName();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(new UserResponse(user));
    }

    // PUT /api/profile/name — update full name
    @PutMapping("/name")
    public ResponseEntity<?> updateName(@RequestBody UpdateNameRequest req,
                                        Authentication authentication) {
        String email = authentication.getName();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (req.fullName() == null || req.fullName().isBlank()) {
            return ResponseEntity.badRequest().body("Name cannot be empty");
        }

        user.setFullName(req.fullName().trim());
        userService.saveUser(user);
        return ResponseEntity.ok(new UserResponse(user));
    }

    // PUT /api/profile/password — change password
    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest req,
                                            Authentication authentication) {
        String email = authentication.getName();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(req.currentPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body("Current password is incorrect");
        }

        if (req.newPassword() == null || req.newPassword().length() < 6) {
            return ResponseEntity.badRequest().body("New password must be at least 6 characters");
        }

        user.setPassword(passwordEncoder.encode(req.newPassword()));
        userService.saveUser(user);
        return ResponseEntity.ok("Password changed successfully");
    }

    // Records for request bodies
    public record UpdateNameRequest(String fullName) {}
    public record ChangePasswordRequest(String currentPassword, String newPassword) {}
}
