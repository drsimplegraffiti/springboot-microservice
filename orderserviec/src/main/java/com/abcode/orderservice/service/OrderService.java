//package com.abcode.orderservice.service;
//
//
//import com.abcode.bookingservice.event.BookingEvent;
//import com.abcode.orderservice.client.InventoryServiceClient;
//import com.abcode.orderservice.entity.Order;
//import com.abcode.orderservice.repository.OrderRepository;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Service;
//
//@Service
//@Slf4j
//public class OrderService {
//
//    private  final OrderRepository orderRepository;
//    private final InventoryServiceClient inventoryServiceClient;
//
//    public OrderService(OrderRepository orderRepository,
//                        InventoryServiceClient inventoryServiceClient) {
//        this.orderRepository = orderRepository;
//        this.inventoryServiceClient = inventoryServiceClient;
//    }
//
//    @KafkaListener(topics = "booking", groupId = "order-service")
//    public void orderEvent(BookingEvent bookingEvent) {
//        log.info("Received order event: {}", bookingEvent);
//        // Create Order object for DB
//        Order order = createOrder(bookingEvent);
//        orderRepository.saveAndFlush(order); // we use saveAndFlush because
//
//        // Update Inventory
//        inventoryServiceClient.updateInventory(order.getEventId(), order.getTicketCount());
//        log.info("Inventory updated for event: {}, less tickets: {}", order.getEventId(), order.getTicketCount());
//    }
//
//
//    private Order createOrder(BookingEvent bookingEvent) {
//        return Order.builder()
//                .customerId(bookingEvent.getUserId())
//                .eventId(bookingEvent.getEventId())
//                .ticketCount(bookingEvent.getTicketCount())
//                .totalPrice(bookingEvent.getTotalPrice())
//                .build();
//    }
//}

package com.abcode.orderservice.service;

import com.abcode.bookingservice.event.BookingEvent;
import com.abcode.orderservice.client.InventoryServiceClient;
import com.abcode.orderservice.entity.Order;
import com.abcode.orderservice.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;

@Service
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryServiceClient inventoryServiceClient;

    public OrderService(OrderRepository orderRepository,
                        InventoryServiceClient inventoryServiceClient) {
        this.orderRepository = orderRepository;
        this.inventoryServiceClient = inventoryServiceClient;
    }

    @KafkaListener(topics = "booking", groupId = "order-service")
    public void orderEvent(BookingEvent bookingEvent) {
        log.info("📥 Received order event: {}", bookingEvent);

        try {
            // Create order entity and save to DB
            Order order = createOrder(bookingEvent);
            orderRepository.saveAndFlush(order);
            log.info("✅ Order saved for customer {}", order.getCustomerId());

            // Call inventory service to update capacity
            inventoryServiceClient.updateInventory(order.getEventId(), order.getTicketCount());
            log.info("📦 Inventory updated for event: {}, -{}", order.getEventId(), order.getTicketCount());

        } catch (HttpServerErrorException e) {
            log.error("❌ Inventory update failed - Server error: {}", e.getResponseBodyAsString(), e);
        } catch (RestClientException e) {
            log.error("❌ Inventory update failed - Client error: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("❌ Unexpected error processing order event: {}", e.getMessage(), e);
        }
    }

    private Order createOrder(BookingEvent bookingEvent) {
        return Order.builder()
                .customerId(bookingEvent.getUserId())
                .eventId(bookingEvent.getEventId())
                .ticketCount(bookingEvent.getTicketCount())
                .totalPrice(bookingEvent.getTotalPrice())
                .build();
    }
}
