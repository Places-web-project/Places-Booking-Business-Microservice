package com.places.booking.service;

import com.places.booking.dto.BookingDtos;
import com.places.booking.dto.PagedResponse;
import com.places.booking.model.Desk;
import com.places.booking.model.Room;
import com.places.booking.repository.DeskRepository;
import com.places.booking.repository.RoomRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class DeskService {

    private final DeskRepository deskRepository;
    private final RoomRepository roomRepository;

    public DeskService(DeskRepository deskRepository, RoomRepository roomRepository) {
        this.deskRepository = deskRepository;
        this.roomRepository = roomRepository;
    }

    public PagedResponse<BookingDtos.DeskResponse> findByRoom(Long roomId, String search, int page, int size) {
        if (!roomRepository.existsById(roomId)) {
            throw new IllegalArgumentException("Room not found");
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by("code").ascending());
        Page<Desk> desks = (search == null || search.isBlank())
                ? deskRepository.findByRoomId(roomId, pageable)
                : deskRepository.searchByRoomIdAndCode(roomId, search, pageable);
        return PagedResponse.of(desks.map(this::toResponse));
    }

    public BookingDtos.DeskResponse findById(Long roomId, Long deskId) {
        Desk desk = deskRepository.findById(deskId)
                .orElseThrow(() -> new IllegalArgumentException("Desk not found"));
        if (!desk.getRoom().getId().equals(roomId)) {
            throw new IllegalArgumentException("Desk does not belong to this room");
        }
        return toResponse(desk);
    }

    public BookingDtos.DeskResponse create(Long roomId, BookingDtos.DeskRequest request) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
        if (deskRepository.existsByCodeAndRoomId(request.code(), roomId)) {
            throw new IllegalArgumentException("A desk with code '" + request.code() + "' already exists in this room");
        }
        Desk desk = new Desk();
        desk.setCode(request.code());
        desk.setDeskType(request.deskType());
        desk.setRoom(room);
        return toResponse(deskRepository.save(desk));
    }

    public BookingDtos.DeskResponse update(Long roomId, Long deskId, BookingDtos.DeskRequest request) {
        Desk desk = deskRepository.findById(deskId)
                .orElseThrow(() -> new IllegalArgumentException("Desk not found"));
        if (!desk.getRoom().getId().equals(roomId)) {
            throw new IllegalArgumentException("Desk does not belong to this room");
        }
        if (!desk.getCode().equals(request.code()) && deskRepository.existsByCodeAndRoomId(request.code(), roomId)) {
            throw new IllegalArgumentException("A desk with code '" + request.code() + "' already exists in this room");
        }
        desk.setCode(request.code());
        desk.setDeskType(request.deskType());
        return toResponse(deskRepository.save(desk));
    }

    public void delete(Long roomId, Long deskId) {
        Desk desk = deskRepository.findById(deskId)
                .orElseThrow(() -> new IllegalArgumentException("Desk not found"));
        if (!desk.getRoom().getId().equals(roomId)) {
            throw new IllegalArgumentException("Desk does not belong to this room");
        }
        deskRepository.deleteById(deskId);
    }

    private BookingDtos.DeskResponse toResponse(Desk desk) {
        return new BookingDtos.DeskResponse(desk.getId(), desk.getCode(), desk.getDeskType(), desk.getRoom().getId());
    }
}
