package com.places.booking.service;

import com.places.booking.dto.BookingDtos;
import com.places.booking.model.Room;
import com.places.booking.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public List<BookingDtos.RoomResponse> findAll(String search) {
        List<Room> rooms = (search == null || search.isBlank())
                ? roomRepository.findAll()
                : roomRepository.searchByName(search);
        return rooms.stream().map(this::toResponse).toList();
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
        roomRepository.deleteById(id);
    }

    private BookingDtos.RoomResponse toResponse(Room room) {
        return new BookingDtos.RoomResponse(room.getId(), room.getName(), room.getCapacity(), room.getRoomType());
    }
}
