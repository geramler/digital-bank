package com.digitalbank.customer.service;

import com.digitalbank.customer.model.Customer;
import com.digitalbank.customer.repository.CustomerRepository;
import com.digitalbank.events.CustomerCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);
    private static final String TOPIC = "customer.created";

    private final CustomerRepository customerRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public CustomerService(CustomerRepository customerRepository, KafkaTemplate<String, Object> kafkaTemplate) {
        this.customerRepository = customerRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public Customer createCustomer(String name, String email) {
        if (customerRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Customer with email '" + email + "' already exists");
        }

        Customer customer = customerRepository.save(new Customer(name, email));

        CustomerCreatedEvent event = CustomerCreatedEvent.of(customer.getId(), customer.getEmail(), customer.getName());
        kafkaTemplate.send(TOPIC, customer.getId().toString(), event)
                .thenAccept(result -> log.info("Published {} to topic {} offset {}", event, TOPIC, result.getRecordMetadata().offset()))
                .exceptionally(ex -> {
                    log.error("Failed to publish customer.created event for customerId={}", customer.getId(), ex);
                    return null;
                });

        log.info("Created customer id={} email={}", customer.getId(), customer.getEmail());
        return customer;
    }
}