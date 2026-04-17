package com.college.eventclub.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eventId;

    @NotBlank(message = "Event title is required")
    @Size(min = 3, message = "Title must be at least 3 characters")
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    private LocalDateTime eventDate;

    @NotBlank(message = "Location is required")
    private String location;

    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    private boolean paid;

    @Min(value = 0, message = "Fee cannot be negative")
    private Double fee;

    @Enumerated(EnumType.STRING)
    private EventStatus status;

    // Feature: Event category tag
    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private EventCategory category = EventCategory.OTHER;

    // Feature: Event banner image URL (stored after upload)
    @Column(name = "image_url")
    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "club_id")
    private Club club;

    public Event() {}

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getEventDate() { return eventDate; }
    public void setEventDate(LocalDateTime eventDate) { this.eventDate = eventDate; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }
    public Double getFee() { return fee; }
    public void setFee(Double fee) { this.fee = fee; }
    public EventStatus getStatus() { return status; }
    public void setStatus(EventStatus status) { this.status = status; }
    public EventCategory getCategory() { return category; }
    public void setCategory(EventCategory category) { this.category = category != null ? category : EventCategory.OTHER; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Club getClub() { return club; }
    public void setClub(Club club) { this.club = club; }
}
