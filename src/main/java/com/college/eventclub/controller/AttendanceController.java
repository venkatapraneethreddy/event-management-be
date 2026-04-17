package com.college.eventclub.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.college.eventclub.model.EventRegistration;
import com.college.eventclub.service.AttendanceService;
import com.college.eventclub.service.EventRegistrationService;

import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final EventRegistrationService registrationService;

    public AttendanceController(AttendanceService attendanceService,
                                EventRegistrationService registrationService) {
        this.attendanceService = attendanceService;
        this.registrationService = registrationService;
    }

    @PostMapping("/scan")
    public ResponseEntity<?> scanQr(@RequestParam String qrCode,
                                    Authentication authentication) {
        try {
            String organizerEmail = authentication.getName();

            EventRegistration registration = registrationService.findByQrCode(qrCode)
                    .orElseThrow(() -> new RuntimeException("Invalid QR code"));

            // Security: ensure the organizer owns the event being scanned
            String eventOrganizerEmail = registration.getEvent()
                    .getClub().getCreatedBy().getEmail();

            if (!eventOrganizerEmail.equals(organizerEmail)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You can only scan attendance for your own events"));
            }

            return new ResponseEntity<>(
                    attendanceService.markAttendance(registration),
                    HttpStatus.CREATED);

        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    // GET attendance count for an event (for organizer's registrants view)
    @GetMapping("/event/{eventId}/count")
    public ResponseEntity<?> getAttendanceCount(@PathVariable Long eventId,
                                                 Authentication authentication) {
        try {
            String organizerEmail = authentication.getName();
            long count = attendanceService.getAttendanceCountForEvent(eventId, organizerEmail);
            return ResponseEntity.ok(Map.of("attendanceCount", count));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", ex.getMessage()));
        }
    }
}
