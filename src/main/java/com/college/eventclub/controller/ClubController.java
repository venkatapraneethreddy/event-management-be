package com.college.eventclub.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.college.eventclub.model.Club;
import com.college.eventclub.model.User;
import com.college.eventclub.service.ClubService;
import com.college.eventclub.service.UserService;

@RestController
@RequestMapping("/api/clubs")
public class ClubController {

    private static final Logger log = LoggerFactory.getLogger(ClubController.class);

    private final ClubService clubService;
    private final UserService userService;

    public ClubController(ClubService clubService, UserService userService) {
        this.clubService = clubService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<Club> createClub(@RequestBody Club club,
                                           Authentication authentication) {
        String email = authentication.getName();

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        club.setCreatedBy(user);
        log.debug("Creating club for user: {}", email); // FIX: replaced System.out.println
        Club savedClub = clubService.createClub(club);
        return new ResponseEntity<>(savedClub, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<?> getApprovedClubs() {
        return ResponseEntity.ok(clubService.getApprovedClubs());
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyClub() {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        Optional<Club> club = clubService.getMyClub(email);

        Map<String, Object> response = new HashMap<>();
        response.put("club", club.orElse(null));
        return ResponseEntity.ok(response);
    }
}
