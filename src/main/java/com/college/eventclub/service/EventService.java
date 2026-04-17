package com.college.eventclub.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.college.eventclub.dto.EventWithSpotsDto;
import com.college.eventclub.dto.OrganizerAnalyticsResponse;
import com.college.eventclub.model.Club;
import com.college.eventclub.model.Event;
import com.college.eventclub.model.EventStatus;
import com.college.eventclub.model.User;
import com.college.eventclub.repository.ClubRepository;
import com.college.eventclub.repository.EventRegistrationRepository;
import com.college.eventclub.repository.EventRepository;
import com.college.eventclub.repository.UserRepository;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final ClubRepository clubRepository;
    private final EventRegistrationRepository eventRegistrationRepository;

    public EventService(EventRepository eventRepository,
                        UserRepository userRepository,
                        ClubRepository clubRepository,
                        EventRegistrationRepository eventRegistrationRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.clubRepository = clubRepository;
        this.eventRegistrationRepository = eventRegistrationRepository;
    }

    @Transactional
    public Event createEventForClub(Event event, Club club, String organizerEmail) {
        if (!club.getCreatedBy().getEmail().equals(organizerEmail)) {
            throw new RuntimeException("You can only post events for your own club");
        }
        event.setClub(club);
        event.setStatus(EventStatus.DRAFT);
        return eventRepository.save(event);
    }

    @Transactional
    public Event publishEvent(Long eventId, String organizerEmail) {
        Event event = getEventById(eventId);
        if (!event.getClub().getCreatedBy().getEmail().equals(organizerEmail)) {
            throw new RuntimeException("You can only publish your own events");
        }
        event.setStatus(EventStatus.PUBLISHED);
        return eventRepository.save(event);
    }

    @Transactional
    public Event adminPublishEvent(Long eventId) {
        Event event = getEventById(eventId);
        event.setStatus(EventStatus.PUBLISHED);
        return eventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public List<EventWithSpotsDto> getPublishedEvents() {
        return eventRepository.findByStatus(EventStatus.PUBLISHED)
                .stream()
                .sorted((a, b) -> {
                    if (a.getEventDate() == null && b.getEventDate() == null) return 0;
                    if (a.getEventDate() == null) return 1;   // nulls last
                    if (b.getEventDate() == null) return -1;
                    return a.getEventDate().compareTo(b.getEventDate());
                })
                .map(e -> new EventWithSpotsDto(e, eventRegistrationRepository.countByEvent(e)))
                .toList();
    }

    @Transactional(readOnly = true)
    public EventWithSpotsDto getEventWithSpots(Long eventId) {
        Event event = getEventById(eventId);
        return new EventWithSpotsDto(event, eventRegistrationRepository.countByEvent(event));
    }

    @Transactional(readOnly = true)
    public List<EventWithSpotsDto> getAllEventsWithSpots() {
        return eventRepository.findAll().stream()
                .map(e -> new EventWithSpotsDto(e, eventRegistrationRepository.countByEvent(e)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Event getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
    }

    @Transactional(readOnly = true)
    public List<EventWithSpotsDto> getEventsByOrganizer(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return clubRepository.findByCreatedBy(user)
                .map(club -> eventRepository.findByClub(club).stream()
                        .map(e -> new EventWithSpotsDto(e, eventRegistrationRepository.countByEvent(e)))
                        .toList())
                .orElse(List.of());
    }

    @Transactional
    public Event updateEvent(Long eventId, Event updates, String organizerEmail) {
        Event event = getEventById(eventId);
        if (!event.getClub().getCreatedBy().getEmail().equals(organizerEmail)) {
            throw new RuntimeException("You can only update your own events");
        }
        if (event.getStatus() == EventStatus.PUBLISHED) {
            if (updates.isPaid() != event.isPaid() ||
                !Objects.equals(updates.getFee(), event.getFee())) {
                throw new RuntimeException("Cannot change payment settings after the event is published");
            }
        }
        if (updates.getTitle() != null)       event.setTitle(updates.getTitle());
        if (updates.getDescription() != null) event.setDescription(updates.getDescription());
        if (updates.getEventDate() != null)   event.setEventDate(updates.getEventDate());
        if (updates.getLocation() != null)    event.setLocation(updates.getLocation());
        if (updates.getCapacity() != null)    event.setCapacity(updates.getCapacity());
        if (updates.getFee() != null)         event.setFee(updates.getFee());
        if (updates.getCategory() != null)    event.setCategory(updates.getCategory());
        event.setPaid(updates.isPaid());
        return eventRepository.save(event);
    }

    @Transactional
    public Event updateEventImage(Long eventId, String imageUrl, String organizerEmail) {
        Event event = getEventById(eventId);
        if (!event.getClub().getCreatedBy().getEmail().equals(organizerEmail)) {
            throw new RuntimeException("Not authorised");
        }
        event.setImageUrl(imageUrl);
        return eventRepository.save(event);
    }

    @Transactional
    public void cancelEvent(Long eventId, String organizerEmail) {
        Event event = getEventById(eventId);
        if (!event.getClub().getCreatedBy().getEmail().equals(organizerEmail)) {
            throw new RuntimeException("You can only cancel your own events");
        }
        event.setStatus(EventStatus.CANCELLED);
        eventRepository.save(event);
    }

    @Transactional
    public void adminCancelEvent(Long eventId) {
        Event event = getEventById(eventId);
        event.setStatus(EventStatus.CANCELLED);
        eventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public OrganizerAnalyticsResponse getOrganizerAnalytics(String organizerEmail) {
        var events = eventRepository.findByClub_CreatedBy_Email(organizerEmail);
        long totalEvents = events.size();
        long totalRegistrations = 0;
        double totalRevenue = 0;
        List<OrganizerAnalyticsResponse.EventStat> stats = new ArrayList<>();
        for (var event : events) {
            long count = eventRegistrationRepository.countByEvent_EventId(event.getEventId());
            // Revenue = confirmed registrations × fee (for paid events)
            double eventRevenue = 0;
            if (event.isPaid() && event.getFee() != null) {
                long confirmedCount = eventRegistrationRepository
                        .countByEventAndStatus(event, com.college.eventclub.model.RegistrationStatus.CONFIRMED);
                eventRevenue = confirmedCount * event.getFee();
            }
            totalRegistrations += count;
            totalRevenue += eventRevenue;
            stats.add(new OrganizerAnalyticsResponse.EventStat(
                    event.getTitle(), count, eventRevenue, event.isPaid()));
        }
        return new OrganizerAnalyticsResponse(totalEvents, totalRegistrations, totalRevenue, stats);
    }
}
