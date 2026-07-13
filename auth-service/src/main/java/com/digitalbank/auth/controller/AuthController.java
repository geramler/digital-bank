package com.digitalbank.auth.controller;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @GetMapping("/health")
    public String health() {
        return "auth-service is up";
    }

    @GetMapping("/info")
    public Map<String, String> info() {
        return Map.of("service", "auth-service", "status", "UP");
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> register(@RequestBody Map<String, String> request) {
        return Map.of("username", request.getOrDefault("username", "unknown"), "status", "REGISTERED");
    }
}
