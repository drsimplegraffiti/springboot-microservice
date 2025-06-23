package com.abcode.inventoryservice.service;
import com.abcode.inventoryservice.entity.Venue;
import com.abcode.inventoryservice.entity.Event;
import com.abcode.inventoryservice.exception.CustomBadRequestException;
import com.abcode.inventoryservice.repository.EventRepository;
import com.abcode.inventoryservice.repository.VenueRepository;
import com.abcode.inventoryservice.response.EventInventoryResponse;
import com.abcode.inventoryservice.response.VenueInventoryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class InventoryService {

    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;

    public InventoryService(EventRepository eventRepository, VenueRepository venueRepository) {
        this.eventRepository = eventRepository;
        this.venueRepository = venueRepository;
    }

    public List<EventInventoryResponse> getAllEvents() {
        return eventRepository.findAll()
                .stream()
                .map(event -> EventInventoryResponse.builder()
                        .event(event.getName())
                        .capacity(event.getTotalCapacity())
                        .venue(event.getVenue())
                        .build())
                .collect(Collectors.toList());
    }

    public VenueInventoryResponse getVenueInformation(Long venueId) {
        final Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new CustomBadRequestException("Venue not found with id: " + venueId));

        return VenueInventoryResponse.builder()
                .venueId(venue.getId())
                .venueName(venue.getName())
                .totalCapacity(venue.getTotalCapacity())
                .build();
    }

    public EventInventoryResponse getEventInventory(Long eventId) {
        final Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new CustomBadRequestException("Event not found with id: " + eventId));
        return EventInventoryResponse.builder()
                .event(event.getName())
                .capacity(event.getTotalCapacity())
                .venue(event.getVenue())
                .ticketPrice(event.getTicketPrice())
                .eventId(event.getId())
                .build();

    }

    public void updateEventCapacity(final Long eventId, final Long ticketsBooked) {
        final Event event = eventRepository.findById(eventId).orElse(null);
        assert event != null;
        event.setLeftCapacity(event.getLeftCapacity() - ticketsBooked);
        eventRepository.saveAndFlush(event);
        log.info("Updated event capacity for event id: {} with tickets booked: {}", eventId, ticketsBooked);
    }
}
