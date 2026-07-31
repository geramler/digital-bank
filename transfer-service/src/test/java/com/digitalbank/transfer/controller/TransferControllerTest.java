package com.digitalbank.transfer.controller;

import com.digitalbank.transfer.saga.TransferSagaOrchestrator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TransferControllerTest {

    private MockMvc mockMvc;
    private TransferSagaOrchestrator sagaOrchestrator;

    @BeforeEach
    void setup() {
        sagaOrchestrator = mock(TransferSagaOrchestrator.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new TransferController(sagaOrchestrator)).build();
    }

    @Test
    void healthEndpointReturnsServiceStatus() throws Exception {
        mockMvc.perform(get("/transfers/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("transfer-service is up"));
    }

    @Test
    void infoEndpointReturnsServiceMetadata() throws Exception {
        mockMvc.perform(get("/transfers/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service").value("transfer-service"))
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void createTransferReturnsAcceptedTransfer() throws Exception {
        mockMvc.perform(post("/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fromAccountId\":1,\"toAccountId\":2,\"amount\":30.0}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }
}
