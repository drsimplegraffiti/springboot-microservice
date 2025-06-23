package com.abcode.bookingservice.controller.service;


import com.abcode.bookingservice.client.InventoryServiceClient;
import com.abcode.bookingservice.controller.entity.Customer;
import com.abcode.bookingservice.controller.repository.CustomerRepository;
import com.abcode.bookingservice.event.BookingEvent;
import com.abcode.bookingservice.exception.CustomBadRequestException;
import com.abcode.bookingservice.request.BookingRequest;
import com.abcode.bookingservice.response.BookingResponse;
import com.abcode.bookingservice.response.InventoryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class BookingService {

    private final CustomerRepository customerRepository;
    private final InventoryServiceClient inventoryServiceClient;
    private final KafkaTemplate<String, BookingEvent> kafkaTemplate;

    public BookingService(CustomerRepository customerRepository, InventoryServiceClient inventoryServiceClient, KafkaTemplate<String, BookingEvent> kafkaTemplate) {
        this.customerRepository = customerRepository;
        this.inventoryServiceClient = inventoryServiceClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    public BookingResponse createBooking(BookingRequest bookingRequest) {
        //check if the user exists
        final Customer customer = customerRepository.findById(bookingRequest.getUserId())
                .orElseThrow(() -> new CustomBadRequestException("User not found"));

        //check if there is enough tickets available for the event
        final InventoryResponse inventoryResponse = inventoryServiceClient.getInventory(bookingRequest.getEventId());
        log.info("Inventory Service Response: {}", inventoryResponse);

        if(inventoryResponse.getCapacity() < bookingRequest.getTicketCount()) {
            throw new CustomBadRequestException("Not enough tickets available for the event");
        }

        final BookingEvent bookingEvent = createBookingEvent(bookingRequest, customer, inventoryResponse);
        //send the booking event to Kafka
        kafkaTemplate.send("booking", bookingEvent);
        log.info("Booking event sent to Kafka: {}", bookingEvent);

        return BookingResponse.builder()
                .userId(customer.getId())
                .eventId(bookingRequest.getEventId())
                .ticketCount(bookingRequest.getTicketCount())
                .totalPrice(inventoryResponse.getTicketPrice().multiply(BigDecimal.valueOf(bookingRequest.getTicketCount())))
                .build();
    }

    private BookingEvent createBookingEvent(BookingRequest bookingRequest, Customer customer, InventoryResponse inventoryResponse) {
        return BookingEvent.builder()
                .userId(customer.getId())
                .eventId(bookingRequest.getEventId())
                .ticketCount(bookingRequest.getTicketCount())
                .totalPrice(inventoryResponse.getTicketPrice().multiply(BigDecimal.valueOf(bookingRequest.getTicketCount())))
                .build();
    }
}
