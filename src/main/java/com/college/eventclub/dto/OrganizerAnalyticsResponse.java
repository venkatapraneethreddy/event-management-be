package com.college.eventclub.dto;

import java.util.List;

public class OrganizerAnalyticsResponse {

    private long totalEvents;
    private long totalRegistrations;
    private double totalRevenue;
    private List<EventStat> eventStats;

    public OrganizerAnalyticsResponse(
            long totalEvents,
            long totalRegistrations,
            double totalRevenue,
            List<EventStat> eventStats) {
        this.totalEvents = totalEvents;
        this.totalRegistrations = totalRegistrations;
        this.totalRevenue = totalRevenue;
        this.eventStats = eventStats;
    }

    public long getTotalEvents() { return totalEvents; }
    public long getTotalRegistrations() { return totalRegistrations; }
    public double getTotalRevenue() { return totalRevenue; }
    public List<EventStat> getEventStats() { return eventStats; }

    public static class EventStat {
        private String eventTitle;
        private long registrations;
        private double revenue;
        private boolean paid;

        public EventStat(String eventTitle, long registrations, double revenue, boolean paid) {
            this.eventTitle = eventTitle;
            this.registrations = registrations;
            this.revenue = revenue;
            this.paid = paid;
        }

        public String getEventTitle() { return eventTitle; }
        public long getRegistrations() { return registrations; }
        public double getRevenue() { return revenue; }
        public boolean isPaid() { return paid; }
    }
}
