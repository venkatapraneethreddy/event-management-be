package com.college.eventclub.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.college.eventclub.dto.EventAnalyticsResponse;
import com.college.eventclub.model.Event;
import com.college.eventclub.service.AnalyticsService;
import com.college.eventclub.service.EventService;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final EventService eventService;

    public AnalyticsController(AnalyticsService analyticsService,
                               EventService eventService) {
        this.analyticsService = analyticsService;
        this.eventService = eventService;
    }

    @GetMapping("/events/{eventId}")
public ResponseEntity<?> eventStats(@PathVariable Long eventId,
                                    Authentication authentication) {

    boolean allowed = authentication.getAuthorities().stream()
            .anyMatch(a ->
                    a.getAuthority().equals("ROLE_ADMIN") ||
                    a.getAuthority().equals("ROLE_ORGANIZER")
            );

    if (!allowed) {
        return ResponseEntity.status(403).build();
    }

    Event event = eventService.getEventById(eventId);

    return ResponseEntity.ok(
            new EventAnalyticsResponse(
                    analyticsService.getTotalRegistrations(event),
                    analyticsService.getTotalAttendance(event)
            )
    );
}


}

