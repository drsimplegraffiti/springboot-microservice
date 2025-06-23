package com.abcode.bookingservice.controller.repository;

import com.abcode.bookingservice.controller.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    // Additional query methods can be defined here if needed
}
