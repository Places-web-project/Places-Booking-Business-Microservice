package com.places.booking.service;

import com.places.booking.model.Booking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class BookingNotificationService {

    private static final Logger log = LoggerFactory.getLogger(BookingNotificationService.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final RestClient authRestClient;
    private final RestClient notificationRestClient;

    public BookingNotificationService(
            RestClient.Builder restClientBuilder,
            @Value("${auth.service.base-url}") String authServiceBaseUrl,
            @Value("${notification.service.base-url}") String notificationServiceBaseUrl
    ) {
        this.authRestClient = restClientBuilder.baseUrl(authServiceBaseUrl).build();
        this.notificationRestClient = restClientBuilder.baseUrl(notificationServiceBaseUrl).build();
    }

    public void sendPendingApprovalNotification(Booking booking, String requesterUsername) {
        List<ManagerContact> recipients = fetchManagerContacts();
        if (recipients.isEmpty()) {
            log.warn("Skipping booking approval email for booking {} because no managers were found", booking.getId());
            return;
        }

        String roomName = booking.getRoom().getName();
        String teamName = booking.getTeam() == null ? "No team selected" : booking.getTeam().getName();
        String subject = "Booking approval needed for " + roomName;
        String body = """
                A new booking requires manager approval.

                Booking ID: %d
                Requested by: %s (user #%d)
                Room: %s
                Team: %s
                Starts at: %s
                Ends at: %s
                Status: %s
                """.formatted(
                booking.getId(),
                requesterUsername,
                booking.getUserId(),
                roomName,
                teamName,
                DATE_TIME_FORMATTER.format(booking.getStartsAt()),
                DATE_TIME_FORMATTER.format(booking.getEndsAt()),
                booking.getStatus().name()
        );

        for (ManagerContact recipient : recipients) {
            if (recipient.email() == null || recipient.email().isBlank()) {
                continue;
            }
            sendEmail(recipient.email(), subject, body, booking.getId());
        }
    }

    private List<ManagerContact> fetchManagerContacts() {
        try {
            ManagerContact[] contacts = authRestClient.get()
                    .uri("/internal/users/manager-contacts")
                    .retrieve()
                    .body(ManagerContact[].class);
            return contacts == null ? List.of() : List.of(contacts);
        } catch (Exception ex) {
            log.warn("Failed to fetch manager contacts: {}", ex.getMessage());
            return List.of();
        }
    }

    private void sendEmail(String recipientEmail, String subject, String body, Long bookingId) {
        try {
            notificationRestClient.post()
                    .uri("/api/notifications/email")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "to", recipientEmail.trim(),
                            "subject", subject,
                            "body", body
                    ))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ex) {
            log.warn("Failed to send booking approval email for booking {} to {}: {}", bookingId, recipientEmail, ex.getMessage());
        }
    }

    private record ManagerContact(Long id, String username, String email) {
    }
}
