package com.places.booking.repository;

import com.places.booking.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByConversationBookingIdOrderByCreatedAtAsc(Long bookingId);
}
