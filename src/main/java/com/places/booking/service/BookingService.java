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

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final TeamRepository teamRepository;

    public BookingService(
            BookingRepository bookingRepository,
            RoomRepository roomRepository,
            TeamRepository teamRepository
    ) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.teamRepository = teamRepository;
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

    public BookingDtos.BookingResponse create(BookingDtos.BookingRequest request) {
        if (request.endsAt().isBefore(request.startsAt()) || request.endsAt().isEqual(request.startsAt())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

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
        booking.setStatus(BookingStatus.PENDING);

        return toResponse(bookingRepository.save(booking));
    }

    public BookingDtos.BookingResponse approve(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalArgumentException("Only PENDING bookings can be approved");
        }
        booking.setStatus(BookingStatus.APPROVED);
        return toResponse(bookingRepository.save(booking));
    }

    public BookingDtos.BookingResponse reject(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalArgumentException("Only PENDING bookings can be rejected");
        }
        booking.setStatus(BookingStatus.REJECTED);
        return toResponse(bookingRepository.save(booking));
    }

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
                booking.getStatus()
        );
    }
}
