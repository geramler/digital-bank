package com.digitalbank.transaction.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TransactionControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TransactionController()).build();
    }

    @Test
    void healthEndpointReturnsServiceStatus() throws Exception {
        mockMvc.perform(get("/transactions/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("transaction-service is up"));
    }

    @Test
    void infoEndpointReturnsServiceMetadata() throws Exception {
        mockMvc.perform(get("/transactions/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service").value("transaction-service"))
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void createTransactionReturnsRecordedTransaction() throws Exception {
        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountId\":1,\"type\":\"DEPOSIT\",\"amount\":50.0}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").value(1))
                .andExpect(jsonPath("$.amount").value(50.0));
    }
}
