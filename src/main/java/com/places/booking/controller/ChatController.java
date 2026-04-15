package com.places.booking.controller;

import com.places.booking.dto.ChatDtos;
import com.places.booking.service.ChatService;
import com.places.booking.service.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final CurrentUserService currentUserService;

    public ChatController(ChatService chatService, CurrentUserService currentUserService) {
        this.chatService = chatService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/bookings/{bookingId}/messages")
    public List<ChatDtos.ChatMessageResponse> getBookingMessages(@PathVariable Long bookingId) {
        Long currentUserId = currentUserService.requireUserId();
        return chatService.getMessages(bookingId, currentUserId);
    }

    @PostMapping("/bookings/{bookingId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public ChatDtos.ChatMessageResponse sendBookingMessage(
            @PathVariable Long bookingId,
            @Valid @RequestBody ChatDtos.CreateMessageRequest request
    ) {
        Long currentUserId = currentUserService.requireUserId();
        return chatService.createMessage(bookingId, currentUserId, request.content(), request.notificationEmail());
    }
}
