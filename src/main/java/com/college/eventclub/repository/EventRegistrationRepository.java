package com.college.eventclub.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.college.eventclub.model.Event;
import com.college.eventclub.model.EventRegistration;
import com.college.eventclub.model.RegistrationStatus;
import com.college.eventclub.model.User;

public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {

    Optional<EventRegistration> findByEventAndUser(Event event, User user);

    long countByEvent(Event event);

    long countByEvent_EventId(Long eventId);

    long countByEventAndStatus(Event event, RegistrationStatus status);

    List<EventRegistration> findByUser(User user);

    Optional<EventRegistration> findByQrCode(String qrCode);

    List<EventRegistration> findByEvent(Event event);

    List<EventRegistration> findByEvent_Club_CreatedBy_Email(String email);
	
	List<EventRegistration> findByEvent_EventId(Long eventId);
}
