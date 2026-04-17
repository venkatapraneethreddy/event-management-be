package com.college.eventclub.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
public class SecurityConfig {

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Bean
    public JwtAuthFilter jwtAuthFilter() {
        return new JwtAuthFilter();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .authorizeHttpRequests(auth -> auth

                // ── PUBLIC ──────────────────────────────────────────────────
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // Serve uploaded images publicly (no login needed to view)
                .requestMatchers("/uploads/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()

                // ── ADMIN ────────────────────────────────────────────────────
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // ── PROFILE (any authenticated user) ─────────────────────────
                .requestMatchers("/api/profile/**").authenticated()

                // ── EVENTS (read: student + organizer + admin) ────────────────
                .requestMatchers(HttpMethod.GET,    "/api/events").permitAll()
                .requestMatchers(HttpMethod.GET,    "/api/events/**").permitAll()
                // Event image upload
                .requestMatchers(HttpMethod.POST,   "/api/events/*/image").hasRole("ORGANIZER")
                // Create event
                .requestMatchers(HttpMethod.POST,   "/api/events/**").hasRole("ORGANIZER")
                // Edit event (organizer edits own, admin can publish)
                .requestMatchers(HttpMethod.PUT,    "/api/events/**").hasAnyRole("ORGANIZER", "ADMIN")
                // Cancel event
                .requestMatchers(HttpMethod.DELETE, "/api/events/**").hasRole("ORGANIZER")

                // ── CLUBS ─────────────────────────────────────────────────────
                .requestMatchers(HttpMethod.GET,    "/api/clubs").hasAnyRole("STUDENT", "ORGANIZER", "ADMIN")
                .requestMatchers(HttpMethod.GET,    "/api/clubs/my").hasRole("ORGANIZER")
                .requestMatchers(HttpMethod.POST,   "/api/clubs").hasRole("ORGANIZER")

                // ── REGISTRATIONS ─────────────────────────────────────────────
                // Organizer: view + export registrants for their events
                .requestMatchers(HttpMethod.GET,    "/api/registrations/event/**").hasRole("ORGANIZER")
                // Student: register, view own, cancel
                .requestMatchers(HttpMethod.POST,   "/api/registrations/**").hasRole("STUDENT")
                .requestMatchers(HttpMethod.GET,    "/api/registrations/my").hasRole("STUDENT")
                .requestMatchers(HttpMethod.DELETE, "/api/registrations/**").hasRole("STUDENT")

                // ── ATTENDANCE ────────────────────────────────────────────────
                .requestMatchers("/api/attendance/**").hasRole("ORGANIZER")

                // ── PAYMENTS ─────────────────────────────────────────────────
                .requestMatchers("/api/payments/**").hasRole("STUDENT")

                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
