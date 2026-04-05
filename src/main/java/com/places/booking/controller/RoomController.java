package com.places.booking.controller;

import com.places.booking.dto.BookingDtos;
import com.places.booking.service.RoomService;
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
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public List<BookingDtos.RoomResponse> getRooms(@RequestParam(required = false) String search) {
        return roomService.findAll(search);
    }

    @GetMapping("/{id}")
    public BookingDtos.RoomResponse getRoom(@PathVariable Long id) {
        return roomService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingDtos.RoomResponse createRoom(@Valid @RequestBody BookingDtos.RoomRequest request) {
        return roomService.create(request);
    }

    @PutMapping("/{id}")
    public BookingDtos.RoomResponse updateRoom(@PathVariable Long id, @Valid @RequestBody BookingDtos.RoomRequest request) {
        return roomService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRoom(@PathVariable Long id) {
        roomService.delete(id);
    }
}
