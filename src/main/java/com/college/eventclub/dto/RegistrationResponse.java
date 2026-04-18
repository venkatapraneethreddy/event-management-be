package com.college.eventclub.dto;

import java.time.LocalDateTime;

public class RegistrationResponse {

    private String studentName;
    private String studentEmail;
    private LocalDateTime registeredAt;
    private String status;

    public RegistrationResponse(String studentName, String studentEmail,
                                LocalDateTime registeredAt, String status) {
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.registeredAt = registeredAt;
        this.status = status;
    }

    public String getStudentName() { return studentName; }
    public String getStudentEmail() { return studentEmail; }
    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public String getStatus() { return status; }
}