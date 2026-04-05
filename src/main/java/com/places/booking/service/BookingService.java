package com.places.booking.service;

import com.places.booking.dto.BookingDtos;
import com.places.booking.model.Booking;
import com.places.booking.model.BookingStatus;
import com.places.booking.model.Room;
import com.places.booking.model.Team;
import com.places.booking.repository.BookingRepository;
import com.places.booking.repository.RoomRepository;
import com.places.booking.repository.TeamRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final TeamRepository teamRepository;

    public BookingService(BookingRepository bookingRepository, RoomRepository roomRepository, TeamRepository teamRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.teamRepository = teamRepository;
    }

    public List<BookingDtos.BookingResponse> findAll(String status) {
        List<Booking> bookings = (status == null || status.isBlank())
                ? bookingRepository.findAll()
                : bookingRepository.findByStatus(BookingStatus.valueOf(status.toUpperCase()));
        return bookings.stream().map(this::toResponse).toList();
    }

    public BookingDtos.BookingResponse create(BookingDtos.BookingRequest request) {
        Room room = roomRepository.findById(request.roomId()).orElseThrow(() -> new IllegalArgumentException("Room not found"));
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
        Booking booking = bookingRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        booking.setStatus(BookingStatus.APPROVED);
        return toResponse(bookingRepository.save(booking));
    }

    public BookingDtos.BookingResponse reject(Long id) {
        Booking booking = bookingRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        booking.setStatus(BookingStatus.REJECTED);
        return toResponse(bookingRepository.save(booking));
    }

    public void delete(Long id) {
        bookingRepository.deleteById(id);
    }

    private BookingDtos.BookingResponse toResponse(Booking booking) {
        return new BookingDtos.BookingResponse(
                booking.getId(),
                booking.getUserId(),
                booking.getRoom().getId(),
                booking.getTeam() == null ? null : booking.getTeam().getId(),
                booking.getStartsAt(),
                booking.getEndsAt(),
                booking.getStatus()
        );
    }
}
