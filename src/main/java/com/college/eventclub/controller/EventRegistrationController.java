package com.college.eventclub.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.college.eventclub.dto.RegistrantResponse;
import com.college.eventclub.model.Event;
import com.college.eventclub.model.User;
import com.college.eventclub.service.EventRegistrationService;
import com.college.eventclub.service.EventService;
import com.college.eventclub.service.UserService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/registrations")
public class EventRegistrationController {

    private final EventRegistrationService eventRegistrationService;
    private final EventService eventService;
    private final UserService userService;

    public EventRegistrationController(EventRegistrationService eventRegistrationService,
                                       EventService eventService,
                                       UserService userService) {
        this.eventRegistrationService = eventRegistrationService;
        this.eventService = eventService;
        this.userService = userService;
    }

    @PostMapping("/{eventId}")
    public ResponseEntity<?> registerForEvent(@PathVariable Long eventId,
                                              Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Event event = eventService.getEventById(eventId);
            return new ResponseEntity<>(
                    eventRegistrationService.register(user, event), HttpStatus.CREATED);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyRegistrations(Authentication authentication) {
        return ResponseEntity.ok(
                eventRegistrationService.getRegistrationsByStudent(authentication.getName()));
    }

    @DeleteMapping("/{registrationId}")
    public ResponseEntity<?> cancelRegistration(@PathVariable Long registrationId,
                                                Authentication authentication) {
        try {
            eventRegistrationService.cancelRegistration(registrationId, authentication.getName());
            return ResponseEntity.ok(Map.of("message", "Registration cancelled successfully"));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<?> getEventRegistrants(@PathVariable Long eventId,
                                                  Authentication authentication) {
        try {
            return ResponseEntity.ok(
                eventRegistrationService.getRegistrantsForEvent(eventId, authentication.getName()));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    // CSV export — GET /api/registrations/event/{eventId}/export
    @GetMapping("/event/{eventId}/export")
    public ResponseEntity<byte[]> exportRegistrantsCsv(@PathVariable Long eventId,
                                                        Authentication authentication) {
        try {
            List<RegistrantResponse> registrants =
                eventRegistrationService.getRegistrantsForEvent(eventId, authentication.getName());

            Event event = eventService.getEventById(eventId);

            StringBuilder csv = new StringBuilder();
            csv.append("Ticket ID,Name,Email,Registration Date,Status\n");
            for (RegistrantResponse r : registrants) {
                csv.append(String.format("#EC-%d,\"%s\",\"%s\",\"%s\",%s\n",
                    r.getRegistrationId(),
                    r.getStudentName(),
                    r.getStudentEmail(),
                    r.getRegisteredAt() != null ? r.getRegisteredAt().toString().replace("T", " ").substring(0, 19) : "",
                    r.getStatus()
                ));
            }

            String filename = "registrants-" + event.getTitle().replaceAll("[^a-zA-Z0-9]", "-") + ".csv";
            byte[] bytes = csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bytes);

        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
