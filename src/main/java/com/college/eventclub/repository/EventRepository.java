package com.college.eventclub.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.college.eventclub.model.Club;
import com.college.eventclub.model.Event;
import com.college.eventclub.model.EventStatus;

public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findByEventIdAndStatus(Long id, EventStatus status);

    List<Event> findByClub(Club club);

    List<Event> findByClub_CreatedBy_Email(String email);

    // FIX: Added missing method for filtering by status
    List<Event> findByStatus(EventStatus status);
}
