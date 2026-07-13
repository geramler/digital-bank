package com.digitalbank.gateway.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GatewayControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(new GatewayController()).build();
    }

    @Test
    void healthEndpointReturnsServiceStatus() throws Exception {
        mockMvc.perform(get("/gateway/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("api-gateway is up"));
    }

    @Test
    void infoEndpointReturnsServiceMetadata() throws Exception {
        mockMvc.perform(get("/gateway/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service").value("api-gateway"))
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
