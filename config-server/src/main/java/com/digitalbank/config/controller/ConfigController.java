package com.digitalbank.config.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/config")
public class ConfigController {

    @GetMapping("/health")
    public String health() {
        return "config-server is up";
    }

    @GetMapping("/info")
    public Map<String, String> info() {
        return Map.of("service", "config-server", "status", "UP");
    }
}
