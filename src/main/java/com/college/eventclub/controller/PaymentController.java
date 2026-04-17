package com.college.eventclub.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.college.eventclub.model.EventRegistration;
import com.college.eventclub.model.User;
import com.college.eventclub.service.EventRegistrationService;
import com.college.eventclub.service.PaymentService;
import com.college.eventclub.service.UserService;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final EventRegistrationService registrationService;
    private final UserService userService;

    public PaymentController(PaymentService paymentService,
                             EventRegistrationService registrationService,
                             UserService userService) {
        this.paymentService = paymentService;
        this.registrationService = registrationService;
        this.userService = userService;
    }

    @PostMapping("/{registrationId}")
    public ResponseEntity<?> pay(@PathVariable Long registrationId,
                                 Authentication authentication) {

        String email = authentication.getName();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        EventRegistration registration =
                registrationService.findById(registrationId)
                        .orElseThrow(() ->
                                new RuntimeException("Registration not found"));

        // ensure user owns registration
        if (!registration.getUser().getUserId()
                .equals(user.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return new ResponseEntity<>(
                paymentService.processPayment(registration),
                HttpStatus.CREATED
        );
    }
}
