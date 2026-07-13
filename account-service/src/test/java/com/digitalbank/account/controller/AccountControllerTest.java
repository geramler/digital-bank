package com.digitalbank.account.controller;

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

class AccountControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AccountController()).build();
    }

    @Test
    void healthEndpointReturnsServiceStatus() throws Exception {
            mockMvc.perform(get("/accounts/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("account-service is up"));
    }

    @Test
    void infoEndpointReturnsServiceMetadata() throws Exception {
            mockMvc.perform(get("/accounts/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service").value("account-service"))
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
        void createAccountEndpointCreatesAccount() throws Exception {
            mockMvc.perform(post("/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"customerId\":1,\"initialBalance\":100.50}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").value(1))
                .andExpect(jsonPath("$.balance").value(100.5));
    }
}
