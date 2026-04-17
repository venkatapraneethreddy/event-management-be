package com.college.eventclub.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.college.eventclub.model.Attendance;
import com.college.eventclub.model.Event;
import com.college.eventclub.model.EventRegistration;   

public interface AttendanceRepository
        extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByRegistration(EventRegistration registration);
    long countByRegistration_Event(Event event);

}
