package com.college.eventclub.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.college.eventclub.model.Club;
import com.college.eventclub.model.ClubStatus;
import com.college.eventclub.repository.ClubRepository;

@Service
public class ClubService {

    private final ClubRepository clubRepository;
    private final EmailService emailService;

    public ClubService(ClubRepository clubRepository, EmailService emailService) {
        this.clubRepository = clubRepository;
        this.emailService = emailService;
    }

    @Transactional
    public Club createClub(Club club) {
        if (club.getCreatedBy() != null &&
            clubRepository.findByCreatedBy(club.getCreatedBy()).isPresent()) {
            throw new RuntimeException("You already have a club. Only one club per organizer is allowed.");
        }
        club.setStatus(ClubStatus.PENDING);
        return clubRepository.save(club);
    }

    @Transactional
    public Club approveClub(Long clubId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new RuntimeException("Club not found"));
        club.setStatus(ClubStatus.APPROVED);
        Club saved = clubRepository.save(club);
        emailService.sendClubApproved(saved);   // ← email notification
        return saved;
    }

    @Transactional
    public Club rejectClub(Long clubId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new RuntimeException("Club not found"));
        club.setStatus(ClubStatus.REJECTED);
        Club saved = clubRepository.save(club);
        emailService.sendClubRejected(saved);   // ← email notification
        return saved;
    }

    @Transactional
    public Club resetClubToPending(Long clubId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new RuntimeException("Club not found"));
        if (club.getStatus() != ClubStatus.REJECTED) {
            throw new RuntimeException("Only rejected clubs can be re-submitted for review");
        }
        club.setStatus(ClubStatus.PENDING);
        return clubRepository.save(club);
    }

    @Transactional(readOnly = true)
    public List<Club> getApprovedClubs() {
        return clubRepository.findByStatus(ClubStatus.APPROVED);
    }

    @Transactional(readOnly = true)
    public List<Club> getPendingClubs() {
        return clubRepository.findByStatus(ClubStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public List<Club> getAllClubs() {
        return clubRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Club> getMyClub(String email) {
        return clubRepository.findByCreatedByEmail(email);
    }
}
