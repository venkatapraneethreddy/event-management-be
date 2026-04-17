package com.college.eventclub.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import com.college.eventclub.model.Club;
import com.college.eventclub.model.Event;
import com.college.eventclub.service.ClubService;
import com.college.eventclub.service.EventService;

import java.util.Map;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private static final Logger log = LoggerFactory.getLogger(EventController.class);

    private final EventService eventService;
    private final ClubService clubService;

    public EventController(EventService eventService, ClubService clubService) {
        this.eventService = eventService;
        this.clubService = clubService;
    }

    @PostMapping("/{clubId}")
    public ResponseEntity<?> createEvent(@PathVariable Long clubId,
                                         Authentication authentication,
                                         @Valid @RequestBody Event event) {
        try {
            String organizerEmail = authentication.getName();
            Club club = clubService.getApprovedClubs().stream()
                    .filter(c -> c.getClubId().equals(clubId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Club not found or not approved"));

            Event savedEvent = eventService.createEventForClub(event, club, organizerEmail);
            return new ResponseEntity<>(savedEvent, HttpStatus.CREATED);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<?> updateEvent(@PathVariable Long eventId,
                                         Authentication authentication,
                                         @RequestBody Event updates) {
        try {
            String email = authentication.getName();
            Event updated = eventService.updateEvent(eventId, updates, email);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    @PutMapping("/{eventId}/publish")
    public ResponseEntity<?> publishEvent(@PathVariable Long eventId,
                                          Authentication authentication) {
        try {
            String email = authentication.getName();
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            Event publishedEvent = isAdmin
                    ? eventService.adminPublishEvent(eventId)
                    : eventService.publishEvent(eventId, email);

            return ResponseEntity.ok(publishedEvent);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<?> cancelEvent(@PathVariable Long eventId,
                                         Authentication authentication) {
        try {
            String email = authentication.getName();
            eventService.cancelEvent(eventId, email);
            return ResponseEntity.ok(Map.of("message", "Event cancelled successfully"));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getPublishedEvents() {
        return ResponseEntity.ok(eventService.getPublishedEvents());
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<?> getEventById(@PathVariable Long eventId) {
        try {
            return ResponseEntity.ok(eventService.getEventWithSpots(eventId));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    // Returns EventWithSpotsDto so organizer sees registrationCount
    @GetMapping("/my")
    public ResponseEntity<?> getMyEvents(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(eventService.getEventsByOrganizer(email));
    }

    @GetMapping("/organizer/analytics")
    public ResponseEntity<?> getAnalytics(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(eventService.getOrganizerAnalytics(email));
    }
}
