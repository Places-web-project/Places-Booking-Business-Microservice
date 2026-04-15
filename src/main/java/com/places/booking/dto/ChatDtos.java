package com.places.booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class ChatDtos {

    public record CreateMessageRequest(
            @NotBlank @Size(max = 1000) String content,
            String notificationEmail
    ) {
    }

    public record ChatMessageResponse(
            Long id,
            Long bookingId,
            Long senderUserId,
            String content,
            LocalDateTime createdAt
    ) {
    }

    public record WebSocketMessageRequest(
            Long bookingId,
            @NotBlank @Size(max = 1000) String content,
            String notificationEmail
    ) {
    }
}
