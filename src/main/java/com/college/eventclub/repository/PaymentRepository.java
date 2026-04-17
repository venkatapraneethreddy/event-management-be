package com.college.eventclub.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.college.eventclub.model.EventRegistration;
import com.college.eventclub.model.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByRegistration(EventRegistration registration);
}
