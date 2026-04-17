package com.college.eventclub.service;

import org.springframework.stereotype.Service;
import java.util.Map;

import com.college.eventclub.repository.UserRepository;
import com.college.eventclub.repository.ClubRepository;
import com.college.eventclub.repository.EventRepository;
import com.college.eventclub.repository.EventRegistrationRepository;
import com.college.eventclub.model.ClubStatus;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final ClubRepository clubRepository;
    private final EventRepository eventRepository;
    private final EventRegistrationRepository eventRegistrationRepository;

    public AdminService(
            UserRepository userRepository,
            ClubRepository clubRepository,
            EventRepository eventRepository,
            EventRegistrationRepository eventRegistrationRepository) {

        this.userRepository = userRepository;
        this.clubRepository = clubRepository;
        this.eventRepository = eventRepository;
        this.eventRegistrationRepository = eventRegistrationRepository;
    }

    public Map<String, Object> getStats() {

        long totalUsers = userRepository.count();
        long totalClubs = clubRepository.count();
        long pendingClubs = clubRepository.countByStatus(ClubStatus.PENDING);
        long totalEvents = eventRepository.count();
        long totalRegistrations = eventRegistrationRepository.count();

        return Map.of(
                "totalUsers", totalUsers,
                "totalClubs", totalClubs,
                "pendingClubs", pendingClubs,
                "totalEvents", totalEvents,
                "totalRegistrations", totalRegistrations
        );
    }
}