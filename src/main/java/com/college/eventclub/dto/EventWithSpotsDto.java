package com.college.eventclub.dto;

import com.college.eventclub.model.Event;
import com.college.eventclub.model.EventCategory;

public class EventWithSpotsDto {

    private final Event event;
    private final long registrationCount;

    public EventWithSpotsDto(Event event, long registrationCount) {
        this.event = event;
        this.registrationCount = registrationCount;
    }

    public Long getEventId()          { return event.getEventId(); }
    public String getTitle()          { return event.getTitle(); }
    public String getDescription()    { return event.getDescription(); }
    public Object getEventDate()      { return event.getEventDate(); }
    public String getPlace()          { return event.getPlace(); }
    public String getLocation()       { return event.getLocation(); }
    public Integer getCapacity()      { return event.getCapacity(); }
    public boolean isPaid()           { return event.isPaid(); }
    public Double getFee()            { return event.getFee(); }
    public Object getStatus()         { return event.getStatus(); }
    public Object getClub()           { return event.getClub(); }
    public EventCategory getCategory(){ return event.getCategory(); }
    public String getImageUrl()       { return event.getImageUrl(); }

    public long getRegistrationCount() { return registrationCount; }

    public Integer getSpotsLeft() {
        if (event.getCapacity() == null) return null;
        return (int) Math.max(0, event.getCapacity() - registrationCount);
    }

    public boolean isFull() {
        if (event.getCapacity() == null) return false;
        return registrationCount >= event.getCapacity();
    }
}