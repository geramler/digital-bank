package com.digitalbank.gateway.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gateway")
public class GatewayController {

    @GetMapping("/health")
    public String health() {
        return "api-gateway is up";
    }

    @GetMapping("/info")
    public Map<String, String> info() {
        return Map.of("service", "api-gateway", "status", "UP");
    }
}
