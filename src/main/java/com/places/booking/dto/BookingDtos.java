package com.places.booking.dto;

import com.places.booking.model.BookingStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class BookingDtos {

    public record RoomRequest(
            @NotNull String name,
            @NotNull Integer capacity,
            @NotNull String roomType
    ) {
    }

    public record RoomResponse(Long id, String name, Integer capacity, String roomType) {
    }

    public record TeamRequest(
            @NotNull String name,
            @NotNull String description
    ) {
    }

    public record TeamResponse(Long id, String name, String description) {
    }

    public record BookingRequest(
            @NotNull Long userId,
            @NotNull Long roomId,
            Long teamId,
            @NotNull @Future LocalDateTime startsAt,
            @NotNull @Future LocalDateTime endsAt
    ) {
    }

    public record BookingResponse(
            Long id,
            Long userId,
            Long roomId,
            Long teamId,
            LocalDateTime startsAt,
            LocalDateTime endsAt,
            BookingStatus status
    ) {
    }
}
