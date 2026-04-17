package com.college.eventclub.service;

import org.springframework.stereotype.Service;

import com.college.eventclub.model.Event;
import com.college.eventclub.repository.AttendanceRepository;
import com.college.eventclub.repository.EventRegistrationRepository;

@Service
public class AnalyticsService {

    private final EventRegistrationRepository registrationRepository;
    private final AttendanceRepository attendanceRepository;

    public AnalyticsService(EventRegistrationRepository registrationRepository,
                            AttendanceRepository attendanceRepository) {
        this.registrationRepository = registrationRepository;
        this.attendanceRepository = attendanceRepository;
    }

    public long getTotalRegistrations(Event event) {
        return registrationRepository.countByEvent(event);
    }

    public long getTotalAttendance(Event event) {
        return attendanceRepository.countByRegistration_Event(event);
    }
}
