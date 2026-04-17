package com.college.eventclub.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.college.eventclub.model.EventRegistration;
import com.college.eventclub.model.Payment;
import com.college.eventclub.model.PaymentStatus;
import com.college.eventclub.model.RegistrationStatus;
import com.college.eventclub.repository.PaymentRepository;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final EventRegistrationService registrationService;
    private final EmailService emailService;

    public PaymentService(PaymentRepository paymentRepository,
                          EventRegistrationService registrationService,
                          EmailService emailService) {
        this.paymentRepository = paymentRepository;
        this.registrationService = registrationService;
        this.emailService = emailService;
    }

    @Transactional
    public Payment processPayment(EventRegistration registration) {
        if (!registration.getEvent().isPaid()) {
            throw new RuntimeException("This event does not require payment");
        }
        if (registration.getStatus() != RegistrationStatus.PENDING_PAYMENT) {
            throw new RuntimeException("Payment already completed or not required");
        }

        Payment payment = new Payment();
        payment.setRegistration(registration);
        payment.setAmount(registration.getEvent().getFee());
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaymentTime(LocalDateTime.now());
        Payment saved = paymentRepository.save(payment);

        EventRegistration confirmed = registrationService.confirmPayment(registration.getRegistrationId());

        // 📧 Send payment confirmation email
        emailService.sendPaymentConfirmed(confirmed);

        return saved;
    }
}
