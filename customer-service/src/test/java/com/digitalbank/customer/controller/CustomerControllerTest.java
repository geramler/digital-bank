package com.digitalbank.customer.controller;

import com.digitalbank.customer.model.Customer;
import com.digitalbank.customer.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CustomerControllerTest {

    private MockMvc mockMvc;
    private CustomerService customerService;

    @BeforeEach
    void setup() {
        customerService = Mockito.mock(CustomerService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new CustomerController(customerService))
                .build();
    }

    @Test
    void healthEndpointReturnsServiceStatus() throws Exception {
        mockMvc.perform(get("/customers/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("customer-service is up"));
    }

    @Test
    void infoEndpointReturnsServiceMetadata() throws Exception {
        mockMvc.perform(get("/customers/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service").value("customer-service"))
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void createCustomerReturnsPersistedCustomer() throws Exception {
        // Given
        Customer savedCustomer = new Customer();
        savedCustomer.setId(UUID.randomUUID());
        savedCustomer.setName("Ada Lovelace");
        savedCustomer.setEmail("ada@example.com");
        savedCustomer.setCreatedAt(LocalDateTime.now());

        when(customerService.createCustomer(anyString(), anyString()))
                .thenReturn(savedCustomer);

        // When & Then
        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Ada Lovelace",
                                    "email": "ada@example.com"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Ada Lovelace"))
                .andExpect(jsonPath("$.email").value("ada@example.com"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.createdAt").exists());
    }
}