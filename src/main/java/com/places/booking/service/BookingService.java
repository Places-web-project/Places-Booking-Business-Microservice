package com.places.booking.service;

import com.places.booking.dto.BookingDtos;
import com.places.booking.dto.PagedResponse;
import com.places.booking.model.Booking;
import com.places.booking.model.BookingStatus;
import com.places.booking.model.Room;
import com.places.booking.model.Team;
import com.places.booking.repository.BookingRepository;
import com.places.booking.repository.RoomRepository;
import com.places.booking.repository.TeamRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class BookingService {
    private static final int FLAGGED_MISSED_CHECKINS_THRESHOLD = 5;

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final TeamRepository teamRepository;
    private final CurrentUserService currentUserService;
    private final LoyaltyService loyaltyService;
    private final BookingNotificationService bookingNotificationService;

    public BookingService(
            BookingRepository bookingRepository,
            RoomRepository roomRepository,
            TeamRepository teamRepository,
            CurrentUserService currentUserService,
            LoyaltyService loyaltyService,
            BookingNotificationService bookingNotificationService
    ) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.teamRepository = teamRepository;
        this.currentUserService = currentUserService;
        this.loyaltyService = loyaltyService;
        this.bookingNotificationService = bookingNotificationService;
    }

    public PagedResponse<BookingDtos.BookingResponse> findAll(String status, Long userId, Long roomId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("startsAt").descending());
        Page<Booking> bookings;

        if (status != null && !status.isBlank()) {
            bookings = bookingRepository.findByStatus(BookingStatus.valueOf(status.toUpperCase()), pageable);
        } else if (userId != null) {
            bookings = bookingRepository.findByUserId(userId, pageable);
        } else if (roomId != null) {
            bookings = bookingRepository.findByRoomId(roomId, pageable);
        } else {
            bookings = bookingRepository.findAll(pageable);
        }

        return PagedResponse.of(bookings.map(this::toResponse));
    }

    public BookingDtos.BookingResponse findById(Long id) {
        return toResponse(bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found")));
    }

    @Transactional
    public BookingDtos.BookingResponse create(BookingDtos.BookingRequest request) {
        if (request.endsAt().isBefore(request.startsAt()) || request.endsAt().isEqual(request.startsAt())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        String requesterUsername = currentUserService.requireUsername();

        Room room = roomRepository.findById(request.roomId())
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
        Team team = request.teamId() == null ? null : teamRepository.findById(request.teamId())
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));

        Booking booking = new Booking();
        booking.setUserId(request.userId());
        booking.setRoom(room);
        booking.setTeam(team);
        booking.setStartsAt(request.startsAt());
        booking.setEndsAt(request.endsAt());
        booking.setStatus(initialStatusForRoom(room));

        Booking saved = bookingRepository.save(booking);
        if (saved.getStatus() == BookingStatus.PENDING) {
            bookingNotificationService.sendPendingApprovalNotification(saved, requesterUsername);
        }

        return toResponse(saved);
    }

    /**
     * Desk bookings are confirmed immediately. Meeting rooms and other shared spaces require manager approval.
     */
    private static BookingStatus initialStatusForRoom(Room room) {
        String raw = room.getRoomType();
        if (raw == null || raw.isBlank()) {
            return BookingStatus.PENDING;
        }
        String t = raw.trim().toUpperCase(Locale.ROOT);
        if ("DESK".equals(t)) {
            return BookingStatus.APPROVED;
        }
        return BookingStatus.PENDING;
    }

    @Transactional
    public BookingDtos.BookingResponse approve(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalArgumentException("Only PENDING bookings can be approved");
        }
        booking.setStatus(BookingStatus.APPROVED);
        return toResponse(bookingRepository.save(booking));
    }

    @Transactional
    public BookingDtos.BookingResponse reject(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalArgumentException("Only PENDING bookings can be rejected");
        }
        booking.setStatus(BookingStatus.REJECTED);
        return toResponse(bookingRepository.save(booking));
    }

    @Transactional
    public BookingDtos.BookingResponse checkIn(Long id) {
        Long currentUserId = currentUserService.requireUserId();
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (!booking.getUserId().equals(currentUserId)) {
            throw new IllegalArgumentException("You can only check in to your own bookings");
        }
        if (booking.getStatus() != BookingStatus.APPROVED) {
            throw new IllegalArgumentException("Only APPROVED bookings can be checked in");
        }
        if (booking.getCheckedInAt() != null) {
            return toResponse(booking);
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(booking.getStartsAt())) {
            throw new IllegalArgumentException("Check-in is only available after the booking starts");
        }
        if (now.isAfter(booking.getEndsAt())) {
            throw new IllegalArgumentException("Check-in is no longer available after the booking ends");
        }

        booking.setCheckedInAt(now);
        Booking saved = bookingRepository.save(booking);
        loyaltyService.recordDeskCheckinEarnIfApplicable(saved);
        return toResponse(saved);
    }

    public BookingDtos.AttendanceSummaryResponse getAttendanceSummaryForCurrentUser() {
        Long currentUserId = currentUserService.requireUserId();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = now.minusDays(30);

        long totalEligibleBookings = bookingRepository.countByUserIdAndStatusAndEndsAtBetween(
                currentUserId,
                BookingStatus.APPROVED,
                from,
                now
        );
        long checkedInCount = bookingRepository.countByUserIdAndStatusAndEndsAtBetweenAndCheckedInAtIsNotNull(
                currentUserId,
                BookingStatus.APPROVED,
                from,
                now
        );
        long missedCheckinsCount = Math.max(totalEligibleBookings - checkedInCount, 0);
        boolean isFlagged = missedCheckinsCount >= FLAGGED_MISSED_CHECKINS_THRESHOLD;

        return new BookingDtos.AttendanceSummaryResponse(
                totalEligibleBookings,
                checkedInCount,
                missedCheckinsCount,
                isFlagged
        );
    }

    @Transactional
    public void delete(Long id) {
        if (!bookingRepository.existsById(id)) {
            throw new IllegalArgumentException("Booking not found");
        }
        bookingRepository.deleteById(id);
    }

    private BookingDtos.BookingResponse toResponse(Booking booking) {
        return new BookingDtos.BookingResponse(
                booking.getId(),
                booking.getUserId(),
                booking.getRoom().getId(),
                booking.getRoom().getName(),
                booking.getTeam() == null ? null : booking.getTeam().getId(),
                booking.getStartsAt(),
                booking.getEndsAt(),
                booking.getStatus(),
                booking.getCheckedInAt()
        );
    }
}
