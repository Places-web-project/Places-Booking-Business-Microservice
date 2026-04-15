package com.places.booking.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.places.booking.dto.ChatDtos;
import com.places.booking.security.JwtService;
import com.places.booking.service.ChatService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final String SESSION_BOOKING_ID = "bookingId";
    private static final String SESSION_USER_ID = "userId";

    private final ChatService chatService;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<Long, Set<WebSocketSession>> sessionsByBooking = new ConcurrentHashMap<>();

    public ChatWebSocketHandler(ChatService chatService, JwtService jwtService, ObjectMapper objectMapper) {
        this.chatService = chatService;
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        URI uri = session.getUri();
        if (uri == null) {
            session.close(CloseStatus.BAD_DATA.withReason("Missing connection metadata"));
            return;
        }

        String token = readQueryParam(uri, "token");
        Long bookingId = parseLong(readQueryParam(uri, "bookingId"));
        if (token == null || token.isBlank() || bookingId == null) {
            session.close(CloseStatus.BAD_DATA.withReason("Missing token or bookingId query parameter"));
            return;
        }

        Long userId;
        try {
            userId = jwtService.extractUserId(token);
        } catch (Exception ex) {
            session.close(CloseStatus.POLICY_VIOLATION.withReason("Invalid token"));
            return;
        }

        if (userId == null) {
            session.close(CloseStatus.POLICY_VIOLATION.withReason("Token is missing userId claim"));
            return;
        }

        try {
            chatService.getMessages(bookingId, userId);
        } catch (IllegalArgumentException ex) {
            session.close(CloseStatus.POLICY_VIOLATION.withReason("Access denied"));
            return;
        }

        session.getAttributes().put(SESSION_BOOKING_ID, bookingId);
        session.getAttributes().put(SESSION_USER_ID, userId);
        sessionsByBooking.computeIfAbsent(bookingId, ignored -> ConcurrentHashMap.newKeySet()).add(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long bookingId = (Long) session.getAttributes().get(SESSION_BOOKING_ID);
        Long userId = (Long) session.getAttributes().get(SESSION_USER_ID);
        if (bookingId == null || userId == null) {
            session.close(CloseStatus.POLICY_VIOLATION.withReason("Connection is not authenticated"));
            return;
        }

        ChatDtos.WebSocketMessageRequest request = parseIncomingMessage(message.getPayload());
        if (request.bookingId() != null && !bookingId.equals(request.bookingId())) {
            session.close(CloseStatus.BAD_DATA.withReason("Booking does not match connection scope"));
            return;
        }

        ChatDtos.ChatMessageResponse saved = chatService.createMessage(
                bookingId,
                userId,
                request.content(),
                request.notificationEmail()
        );
        broadcast(bookingId, toJson(saved));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        removeSession(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        removeSession(session);
    }

    private void broadcast(Long bookingId, String payload) {
        Set<WebSocketSession> sessions = sessionsByBooking.getOrDefault(bookingId, Set.of());
        for (WebSocketSession session : sessions) {
            if (!session.isOpen()) {
                continue;
            }
            try {
                session.sendMessage(new TextMessage(payload));
            } catch (IOException ex) {
                removeSession(session);
            }
        }
    }

    private void removeSession(WebSocketSession session) {
        Long bookingId = (Long) session.getAttributes().get(SESSION_BOOKING_ID);
        if (bookingId == null) {
            return;
        }
        Set<WebSocketSession> sessions = sessionsByBooking.get(bookingId);
        if (sessions == null) {
            return;
        }
        sessions.remove(session);
        if (sessions.isEmpty()) {
            sessionsByBooking.remove(bookingId);
        }
    }

    private ChatDtos.WebSocketMessageRequest parseIncomingMessage(String payload) throws IOException {
        ChatDtos.WebSocketMessageRequest request = objectMapper.readValue(payload, ChatDtos.WebSocketMessageRequest.class);
        if (request.content() == null || request.content().isBlank()) {
            throw new IllegalArgumentException("Message content cannot be empty");
        }
        return request;
    }

    private String toJson(ChatDtos.ChatMessageResponse message) throws JsonProcessingException {
        return objectMapper.writeValueAsString(message);
    }

    private String readQueryParam(URI uri, String key) {
        return UriComponentsBuilder.fromUri(uri)
                .build()
                .getQueryParams()
                .getFirst(key);
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
