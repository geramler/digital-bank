package com.digitalbank.notification.controller;

import com.digitalbank.notification.model.Notification;
import com.digitalbank.notification.repository.NotificationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @GetMapping("/health")
    public String health() {
        return "notification-service is up";
    }

    @GetMapping("/info")
    public Map<String, String> info() {
        return Map.of("service", "notification-service", "status", "UP");
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createNotification(@RequestBody Map<String, String> request) {
        String recipient = request.getOrDefault("recipient", "unknown@example.com");
        String message = request.getOrDefault("message", "");
        String channel = request.getOrDefault("channel", "EMAIL");

        Notification notification = notificationRepository.save(
                new Notification(recipient, message, channel, "MANUAL"));

        return Map.of(
            "id", notification.getId(),
            "recipient", notification.getRecipient(),
            "message", notification.getMessage(),
            "channel", notification.getChannel(),
            "createdAt", notification.getCreatedAt().toString()
        );
    }
}