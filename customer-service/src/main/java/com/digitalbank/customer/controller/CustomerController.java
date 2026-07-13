package com.digitalbank.customer.controller;

import com.digitalbank.customer.model.Customer;
import com.digitalbank.customer.service.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/health")
    public String health() {
        return "customer-service is up";
    }

    @GetMapping("/info")
    public Map<String, String> info() {
        return Map.of("service", "customer-service", "status", "UP");
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createCustomer(@RequestBody Map<String, String> request) {
        String name = request.getOrDefault("name", "Unknown");
        String email = request.getOrDefault("email", "unknown@example.com");

        Customer customer = customerService.createCustomer(name, email);

        return Map.of(
            "id", customer.getId(),
            "name", customer.getName(),
            "email", customer.getEmail(),
            "createdAt", customer.getCreatedAt().toString()
        );
    }
}