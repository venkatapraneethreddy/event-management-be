package com.college.eventclub.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.college.eventclub.model.Club;
import com.college.eventclub.service.AdminService;
import com.college.eventclub.service.ClubService;
import com.college.eventclub.service.EventService;
import com.college.eventclub.service.UserService;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final ClubService clubService;
    private final AdminService adminService;
    private final EventService eventService;
    private final UserService userService;

    public AdminController(ClubService clubService, AdminService adminService,
                           EventService eventService, UserService userService) {
        this.clubService = clubService;
        this.adminService = adminService;
        this.eventService = eventService;
        this.userService = userService;
    }

    // ── Club management ───────────────────────────────────────────────────────

    @PutMapping("/clubs/{id}/approve")
    public ResponseEntity<Club> approveClub(@PathVariable Long id) {
        return ResponseEntity.ok(clubService.approveClub(id));
    }

    @PutMapping("/clubs/{id}/reject")
    public ResponseEntity<Club> rejectClub(@PathVariable Long id) {
        return ResponseEntity.ok(clubService.rejectClub(id));
    }

    @PutMapping("/clubs/{id}/reset")
    public ResponseEntity<?> resetClub(@PathVariable Long id) {
        return ResponseEntity.ok(clubService.resetClubToPending(id));
    }

    @GetMapping("/clubs/pending")
    public ResponseEntity<?> getPendingClubs() {
        return ResponseEntity.ok(clubService.getPendingClubs());
    }

    @GetMapping("/clubs")
    public ResponseEntity<?> getAllClubs() {
        return ResponseEntity.ok(clubService.getAllClubs());
    }

    // ── Event management (admin view) ─────────────────────────────────────────

    @GetMapping("/events")
    public ResponseEntity<?> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEventsWithSpots());
    }

    @PutMapping("/events/{eventId}/publish")
    public ResponseEntity<?> publishEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(eventService.adminPublishEvent(eventId));
    }

    @DeleteMapping("/events/{eventId}")
    public ResponseEntity<?> cancelEvent(@PathVariable Long eventId) {
        try {
            eventService.adminCancelEvent(eventId);
            return ResponseEntity.ok(Map.of("message", "Event cancelled"));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    // ── Stats ─────────────────────────────────────────────────────────────────

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    // ── User management ───────────────────────────────────────────────────────

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    /** GET /api/admin/users/export — download all users as CSV */
    @GetMapping("/users/export")
    public ResponseEntity<byte[]> exportUsersCsv() {
        var users = userService.getAllUsers();
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Full Name,Email,Role,Member Since\n");
        for (var u : users) {
            String since = u.getCreatedAt() != null
                ? u.getCreatedAt().toLocalDate().toString() : "";
            csv.append(String.format("%d,\"%s\",\"%s\",%s,%s\n",
                u.getUserId(), u.getFullName(), u.getEmail(),
                u.getRole(), since));
        }
        byte[] bytes = csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return ResponseEntity.ok()
            .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"eventclub-users.csv\"")
            .contentType(org.springframework.http.MediaType.parseMediaType("text/csv"))
            .body(bytes);
    }
}
