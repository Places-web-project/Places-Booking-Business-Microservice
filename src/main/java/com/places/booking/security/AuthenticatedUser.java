package com.places.booking.security;

public record AuthenticatedUser(Long userId, String username) {
}
