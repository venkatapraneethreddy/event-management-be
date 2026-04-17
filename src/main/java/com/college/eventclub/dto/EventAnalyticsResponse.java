package com.college.eventclub.dto;

public class EventAnalyticsResponse {

    private long registrations;
    private long attendance;

    public EventAnalyticsResponse(long registrations, long attendance) {
        this.registrations = registrations;
        this.attendance = attendance;
    }

    public long getRegistrations() {
        return registrations;
    }

    public long getAttendance() {
        return attendance;
    }
}
