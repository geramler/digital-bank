package com.digitalbank.customer.service;

import com.digitalbank.customer.model.Customer;
import com.digitalbank.customer.outbox.OutboxEvent;
import com.digitalbank.customer.outbox.OutboxEventRepository;
import com.digitalbank.customer.repository.CustomerRepository;
import com.digitalbank.events.CustomerCreatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);
    private static final String TOPIC = "customer.created";

    private final CustomerRepository customerRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public CustomerService(
            CustomerRepository customerRepository,
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper) {
        this.customerRepository = customerRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Customer createCustomer(String name, String email) {
        if (customerRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Customer with email '" + email + "' already exists");
        }

        Customer customer = customerRepository.save(new Customer(name, email));

        CustomerCreatedEvent event = CustomerCreatedEvent.of(customer.getId(), customer.getEmail(), customer.getName());
        outboxEventRepository.save(OutboxEvent.of(TOPIC, customer.getId().toString(), event, objectMapper));

        log.info("Created customer id={} email={}", customer.getId(), customer.getEmail());
        return customer;
    }
}
