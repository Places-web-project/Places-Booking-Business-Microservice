package com.places.booking.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ChatNotificationService {

    private static final Logger log = LoggerFactory.getLogger(ChatNotificationService.class);

    private final RestClient restClient;

    public ChatNotificationService(
            RestClient.Builder restClientBuilder,
            @Value("${notification.service.base-url}") String notificationBaseUrl
    ) {
        this.restClient = restClientBuilder.baseUrl(notificationBaseUrl).build();
    }

    public void sendMessageNotification(String email, Long bookingId, String content) {
        if (email == null || email.isBlank()) {
            return;
        }

        String compactBody = content.length() > 220 ? content.substring(0, 220) + "..." : content;
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("to", email.trim());
        payload.put("subject", "New chat message for booking #" + bookingId);
        payload.put("body", compactBody);

        try {
            restClient.post()
                    .uri("/api/notifications/email")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ex) {
            log.warn("Failed to send chat notification email for booking {}: {}", bookingId, ex.getMessage());
        }
    }
}
