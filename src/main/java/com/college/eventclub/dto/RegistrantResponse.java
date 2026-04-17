package com.college.eventclub.dto;

import com.college.eventclub.model.EventRegistration;
import com.college.eventclub.model.RegistrationStatus;
import java.time.LocalDateTime;

public class RegistrantResponse {

    private Long registrationId;
    private String studentName;
    private String studentEmail;
    private LocalDateTime registeredAt;
    private RegistrationStatus status;
    private String qrCode;

    public RegistrantResponse(EventRegistration reg) {
        this.registrationId = reg.getRegistrationId();
        this.studentName = reg.getUser().getFullName();
        this.studentEmail = reg.getUser().getEmail();
        this.registeredAt = reg.getRegisteredAt();
        this.status = reg.getStatus();
        this.qrCode = reg.getQrCode();
    }

    public Long getRegistrationId() { return registrationId; }
    public String getStudentName() { return studentName; }
    public String getStudentEmail() { return studentEmail; }
    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public RegistrationStatus getStatus() { return status; }
    public String getQrCode() { return qrCode; }
}
