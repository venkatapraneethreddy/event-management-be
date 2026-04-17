package com.college.eventclub.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.Map;

import com.college.eventclub.config.JwtUtil;
import com.college.eventclub.dto.LoginRequest;
import com.college.eventclub.dto.RegisterResponse;
import com.college.eventclub.model.Role;
import com.college.eventclub.model.User;
import com.college.eventclub.service.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserService userService,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody User user) {
        // Prevent self-registration as ADMIN
        if (user.getRole() == null || user.getRole() == Role.ADMIN) {
            user.setRole(Role.STUDENT);
        }

        // Check for duplicate email
        if (userService.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "An account with this email already exists"));
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userService.saveUser(user);

        return new ResponseEntity<>(
            new RegisterResponse(
                savedUser.getUserId(),
                savedUser.getFullName(),
                savedUser.getEmail(),
                savedUser.getRole()
            ),
            HttpStatus.CREATED
        );
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        return userService.findByEmail(request.getEmail())
            .map(user -> {
                if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                    String token = jwtUtil.generateToken(
                            user.getEmail(),
                            user.getRole().name());

                    return ResponseEntity.ok(
                        Map.of(
                            "token", token,
                            "role", user.getRole().name(),
                            "userId", user.getUserId(),
                            "fullName", user.getFullName()
                        )
                    );
                } else {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Map.of("error", "Invalid credentials"));
                }
            })
            .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No account found with this email")));
    }
}
