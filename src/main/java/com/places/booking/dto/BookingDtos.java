package com.places.booking.dto;

import com.places.booking.model.BookingStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public class BookingDtos {

    public record RoomRequest(
            @NotBlank String name,
            @NotNull @Positive Integer capacity,
            @NotBlank String roomType
    ) {
    }

    public record RoomResponse(Long id, String name, Integer capacity, String roomType) {
    }

    public record DeskRequest(
            @NotBlank String code,
            @NotBlank String deskType
    ) {
    }

    public record DeskResponse(Long id, String code, String deskType, Long roomId) {
    }

    public record TeamRequest(
            @NotBlank String name,
            @NotBlank String description
    ) {
    }

    public record TeamResponse(Long id, String name, String description) {
    }

    public record TeamMemberRequest(
            @NotNull Long userId,
            @NotBlank String displayName
    ) {
    }

    public record TeamMemberResponse(Long id, Long userId, String displayName, Long teamId) {
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
            String roomName,
            Long teamId,
            LocalDateTime startsAt,
            LocalDateTime endsAt,
            BookingStatus status
    ) {
    }
}
