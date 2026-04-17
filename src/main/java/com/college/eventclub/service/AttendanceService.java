package com.college.eventclub.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.college.eventclub.model.Attendance;
import com.college.eventclub.model.EventRegistration;
import com.college.eventclub.model.RegistrationStatus;
import com.college.eventclub.repository.AttendanceRepository;
import com.college.eventclub.repository.EventRegistrationRepository;
import com.college.eventclub.repository.EventRepository;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EventRegistrationRepository registrationRepository;
    private final EventRepository eventRepository;

    public AttendanceService(AttendanceRepository attendanceRepository,
                             EventRegistrationRepository registrationRepository,
                             EventRepository eventRepository) {
        this.attendanceRepository = attendanceRepository;
        this.registrationRepository = registrationRepository;
        this.eventRepository = eventRepository;
    }

    public Attendance markAttendance(EventRegistration registration) {
        if (registration.getStatus() != RegistrationStatus.CONFIRMED) {
            throw new RuntimeException(
                "Cannot mark attendance: registration is not confirmed. Payment may be pending.");
        }

        attendanceRepository.findByRegistration(registration)
                .ifPresent(a -> {
                    throw new RuntimeException("Attendance already marked for this ticket");
                });

        Attendance attendance = new Attendance();
        attendance.setRegistration(registration);
        attendance.setCheckInTime(LocalDateTime.now());

        return attendanceRepository.save(attendance);
    }

    public long getAttendanceCountForEvent(Long eventId, String organizerEmail) {
        var event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (!event.getClub().getCreatedBy().getEmail().equals(organizerEmail)) {
            throw new RuntimeException("Not authorized to view attendance for this event");
        }

        return registrationRepository.findByEvent(event).stream()
                .filter(reg -> attendanceRepository.findByRegistration(reg).isPresent())
                .count();
    }
}
