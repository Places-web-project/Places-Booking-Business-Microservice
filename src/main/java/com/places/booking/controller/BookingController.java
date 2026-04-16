package com.places.booking.controller;

import com.places.booking.dto.BookingDtos;
import com.places.booking.dto.PagedResponse;
import com.places.booking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public PagedResponse<BookingDtos.BookingResponse> getBookings(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return bookingService.findAll(status, userId, roomId, page, size);
    }

    @GetMapping("/{id}")
    public BookingDtos.BookingResponse getBooking(@PathVariable Long id) {
        return bookingService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingDtos.BookingResponse createBooking(@Valid @RequestBody BookingDtos.BookingRequest request) {
        return bookingService.create(request);
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public BookingDtos.BookingResponse approve(@PathVariable Long id) {
        return bookingService.approve(id);
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public BookingDtos.BookingResponse reject(@PathVariable Long id) {
        return bookingService.reject(id);
    }

    @PutMapping("/{id}/check-in")
    public BookingDtos.BookingResponse checkIn(@PathVariable Long id) {
        return bookingService.checkIn(id);
    }

    @GetMapping("/me/attendance-summary")
    public BookingDtos.AttendanceSummaryResponse getAttendanceSummary() {
        return bookingService.getAttendanceSummaryForCurrentUser();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public void deleteBooking(@PathVariable Long id) {
        bookingService.delete(id);
    }
}
