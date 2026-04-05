package com.places.booking.controller;

import com.places.booking.dto.BookingDtos;
import com.places.booking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public List<BookingDtos.BookingResponse> getBookings(@RequestParam(required = false) String status) {
        return bookingService.findAll(status);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingDtos.BookingResponse createBooking(@Valid @RequestBody BookingDtos.BookingRequest request) {
        return bookingService.create(request);
    }

    @PutMapping("/{id}/approve")
    public BookingDtos.BookingResponse approve(@PathVariable Long id) {
        return bookingService.approve(id);
    }

    @PutMapping("/{id}/reject")
    public BookingDtos.BookingResponse reject(@PathVariable Long id) {
        return bookingService.reject(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBooking(@PathVariable Long id) {
        bookingService.delete(id);
    }
}
