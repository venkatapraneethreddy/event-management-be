package com.college.eventclub.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "attendance",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"registration_id"})
       })
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long attendanceId;

    @OneToOne
    @JoinColumn(name = "registration_id", nullable = false)
    private EventRegistration registration;

    private LocalDateTime checkInTime;

    public Attendance() {}

    // getters & setters
    public Long getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(Long attendanceId) {
        this.attendanceId = attendanceId;
    }

    public EventRegistration getRegistration() {
        return registration;
    }

    public void setRegistration(EventRegistration registration) {
        this.registration = registration;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }
}
