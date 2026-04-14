package com.places.booking.controller;

import com.places.booking.dto.BookingDtos;
import com.places.booking.dto.PagedResponse;
import com.places.booking.service.DeskService;
import com.places.booking.service.RoomService;
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
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;
    private final DeskService deskService;

    public RoomController(RoomService roomService, DeskService deskService) {
        this.roomService = roomService;
        this.deskService = deskService;
    }

    @GetMapping
    public PagedResponse<BookingDtos.RoomResponse> getRooms(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy
    ) {
        return roomService.findAll(search, page, size, sortBy);
    }

    @GetMapping("/{id}")
    public BookingDtos.RoomResponse getRoom(@PathVariable Long id) {
        return roomService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public BookingDtos.RoomResponse createRoom(@Valid @RequestBody BookingDtos.RoomRequest request) {
        return roomService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public BookingDtos.RoomResponse updateRoom(@PathVariable Long id, @Valid @RequestBody BookingDtos.RoomRequest request) {
        return roomService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteRoom(@PathVariable Long id) {
        roomService.delete(id);
    }

    // ---- Desks nested under a room ----

    @GetMapping("/{roomId}/desks")
    public PagedResponse<BookingDtos.DeskResponse> getDesks(
            @PathVariable Long roomId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return deskService.findByRoom(roomId, search, page, size);
    }

    @GetMapping("/{roomId}/desks/{deskId}")
    public BookingDtos.DeskResponse getDesk(@PathVariable Long roomId, @PathVariable Long deskId) {
        return deskService.findById(roomId, deskId);
    }

    @PostMapping("/{roomId}/desks")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public BookingDtos.DeskResponse createDesk(
            @PathVariable Long roomId,
            @Valid @RequestBody BookingDtos.DeskRequest request
    ) {
        return deskService.create(roomId, request);
    }

    @PutMapping("/{roomId}/desks/{deskId}")
    @PreAuthorize("hasRole('ADMIN')")
    public BookingDtos.DeskResponse updateDesk(
            @PathVariable Long roomId,
            @PathVariable Long deskId,
            @Valid @RequestBody BookingDtos.DeskRequest request
    ) {
        return deskService.update(roomId, deskId, request);
    }

    @DeleteMapping("/{roomId}/desks/{deskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteDesk(@PathVariable Long roomId, @PathVariable Long deskId) {
        deskService.delete(roomId, deskId);
    }
}
