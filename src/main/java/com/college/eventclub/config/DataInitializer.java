package com.college.eventclub.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.college.eventclub.model.Role;
import com.college.eventclub.model.User;
import com.college.eventclub.service.UserService;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initAdmin(UserService userService, PasswordEncoder passwordEncoder) {
        return args -> {
			System.out.println("🔥 DataInitializer running...");
            String adminEmail = "admin@gmail.com";

            if (userService.findByEmail(adminEmail).isEmpty()) {
                User admin = new User();
                admin.setFullName("Admin");
                admin.setEmail(adminEmail);
                admin.setPassword(passwordEncoder.encode("Admin@123"));
                admin.setRole(Role.ADMIN);

                userService.saveUser(admin);

                System.out.println("✅ Admin created: " + adminEmail);
            } else {
                System.out.println("ℹ️ Admin already exists");
            }
        };
    }
}