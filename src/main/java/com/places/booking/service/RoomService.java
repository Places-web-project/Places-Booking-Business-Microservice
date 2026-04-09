package com.places.booking.service;

import com.places.booking.dto.BookingDtos;
import com.places.booking.dto.PagedResponse;
import com.places.booking.model.Room;
import com.places.booking.repository.RoomRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public PagedResponse<BookingDtos.RoomResponse> findAll(String search, int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        Page<Room> rooms = (search == null || search.isBlank())
                ? roomRepository.findAll(pageable)
                : roomRepository.searchByName(search, pageable);
        return PagedResponse.of(rooms.map(this::toResponse));
    }

    public BookingDtos.RoomResponse findById(Long id) {
        return toResponse(roomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Room not found")));
    }

    public BookingDtos.RoomResponse create(BookingDtos.RoomRequest request) {
        Room room = new Room();
        room.setName(request.name());
        room.setCapacity(request.capacity());
        room.setRoomType(request.roomType());
        return toResponse(roomRepository.save(room));
    }

    public BookingDtos.RoomResponse update(Long id, BookingDtos.RoomRequest request) {
        Room room = roomRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Room not found"));
        room.setName(request.name());
        room.setCapacity(request.capacity());
        room.setRoomType(request.roomType());
        return toResponse(roomRepository.save(room));
    }

    public void delete(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new IllegalArgumentException("Room not found");
        }
        roomRepository.deleteById(id);
    }

    public BookingDtos.RoomResponse toResponse(Room room) {
        return new BookingDtos.RoomResponse(room.getId(), room.getName(), room.getCapacity(), room.getRoomType());
    }
}
