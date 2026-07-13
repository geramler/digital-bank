package com.digitalbank.notification.controller;

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

class NotificationControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(new NotificationController()).build();
    }

    @Test
    void healthEndpointReturnsServiceStatus() throws Exception {
        mockMvc.perform(get("/notifications/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("notification-service is up"));
    }

    @Test
    void infoEndpointReturnsServiceMetadata() throws Exception {
        mockMvc.perform(get("/notifications/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service").value("notification-service"))
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void createNotificationReturnsPersistedNotification() throws Exception {
        mockMvc.perform(post("/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recipient\":\"alice@example.com\",\"message\":\"Welcome\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.recipient").value("alice@example.com"));
    }
}
