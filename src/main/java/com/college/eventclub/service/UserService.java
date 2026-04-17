package com.college.eventclub.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.college.eventclub.dto.UserResponse;
import com.college.eventclub.model.User;
import com.college.eventclub.repository.AttendanceRepository;
import com.college.eventclub.repository.ClubRepository;
import com.college.eventclub.repository.EventRegistrationRepository;
import com.college.eventclub.repository.EventRepository;
import com.college.eventclub.repository.PaymentRepository;
import com.college.eventclub.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final EventRegistrationRepository registrationRepository;
    private final AttendanceRepository attendanceRepository;
    private final EventRepository eventRepository;
    private final ClubRepository clubRepository;
    private final PaymentRepository paymentRepository;

    public UserService(UserRepository userRepository,
                       EventRegistrationRepository registrationRepository,
                       AttendanceRepository attendanceRepository,
                       EventRepository eventRepository,
                       ClubRepository clubRepository,
                       PaymentRepository paymentRepository) {
        this.userRepository = userRepository;
        this.registrationRepository = registrationRepository;
        this.attendanceRepository = attendanceRepository;
        this.eventRepository = eventRepository;
        this.clubRepository = clubRepository;
        this.paymentRepository = paymentRepository;
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserResponse::new)
                .toList();
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 1. Delete attendance records for this user's registrations
        var registrations = registrationRepository.findByUser(user);
        for (var reg : registrations) {
            attendanceRepository.findByRegistration(reg)
                    .ifPresent(attendanceRepository::delete);
            // Delete payment if exists
            paymentRepository.findByRegistration(reg)
                    .ifPresent(paymentRepository::delete);
        }

        // 2. Delete this user's registrations
        registrationRepository.deleteAll(registrations);

        // 3. If organizer — delete their club's events and then the club
        clubRepository.findByCreatedBy(user).ifPresent(club -> {
            var events = eventRepository.findByClub(club);
            for (var event : events) {
                // Delete all registrations (and their attendance/payments) for each event
                var eventRegs = registrationRepository.findByEvent(event);
                for (var reg : eventRegs) {
                    attendanceRepository.findByRegistration(reg)
                            .ifPresent(attendanceRepository::delete);
                    paymentRepository.findByRegistration(reg)
                            .ifPresent(paymentRepository::delete);
                }
                registrationRepository.deleteAll(eventRegs);
            }
            eventRepository.deleteAll(events);
            clubRepository.delete(club);
        });

        // 4. Finally delete the user
        userRepository.delete(user);
    }
}
