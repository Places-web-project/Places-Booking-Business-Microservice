package com.places.booking.service;

import com.places.booking.dto.ChatDtos;
import com.places.booking.model.Booking;
import com.places.booking.model.ChatConversation;
import com.places.booking.model.ChatMessage;
import com.places.booking.repository.BookingRepository;
import com.places.booking.repository.ChatConversationRepository;
import com.places.booking.repository.ChatMessageRepository;
import com.places.booking.repository.TeamMemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService {

    private final BookingRepository bookingRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final ChatConversationRepository chatConversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatNotificationService chatNotificationService;

    public ChatService(
            BookingRepository bookingRepository,
            TeamMemberRepository teamMemberRepository,
            ChatConversationRepository chatConversationRepository,
            ChatMessageRepository chatMessageRepository,
            ChatNotificationService chatNotificationService
    ) {
        this.bookingRepository = bookingRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.chatConversationRepository = chatConversationRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.chatNotificationService = chatNotificationService;
    }

    @Transactional(readOnly = true)
    public List<ChatDtos.ChatMessageResponse> getMessages(Long bookingId, Long currentUserId) {
        authorizeBookingParticipant(bookingId, currentUserId);
        return chatMessageRepository.findByConversationBookingIdOrderByCreatedAtAsc(bookingId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ChatDtos.ChatMessageResponse createMessage(
            Long bookingId,
            Long currentUserId,
            String content,
            String notificationEmail
    ) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Message content cannot be empty");
        }

        Booking booking = authorizeBookingParticipant(bookingId, currentUserId);
        ChatConversation conversation = findOrCreateConversation(booking);

        ChatMessage message = new ChatMessage();
        message.setConversation(conversation);
        message.setSenderUserId(currentUserId);
        message.setContent(content.trim());
        message.setCreatedAt(LocalDateTime.now());

        ChatMessage saved = chatMessageRepository.save(message);
        chatNotificationService.sendMessageNotification(notificationEmail, bookingId, saved.getContent());
        return toResponse(saved);
    }

    private Booking authorizeBookingParticipant(Long bookingId, Long currentUserId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (booking.getUserId().equals(currentUserId)) {
            return booking;
        }

        if (booking.getTeam() != null && teamMemberRepository.existsByUserIdAndTeamId(currentUserId, booking.getTeam().getId())) {
            return booking;
        }

        throw new IllegalArgumentException("You are not allowed to access this booking conversation");
    }

    private ChatConversation findOrCreateConversation(Booking booking) {
        return chatConversationRepository.findByBookingId(booking.getId())
                .orElseGet(() -> {
                    ChatConversation conversation = new ChatConversation();
                    conversation.setBooking(booking);
                    conversation.setCreatedAt(LocalDateTime.now());
                    return chatConversationRepository.save(conversation);
                });
    }

    private ChatDtos.ChatMessageResponse toResponse(ChatMessage message) {
        return new ChatDtos.ChatMessageResponse(
                message.getId(),
                message.getConversation().getBooking().getId(),
                message.getSenderUserId(),
                message.getContent(),
                message.getCreatedAt()
        );
    }
}
