package com.places.booking.service;

import com.places.booking.security.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    public Long requireUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalArgumentException("Authentication is required");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthenticatedUser authenticatedUser && authenticatedUser.userId() != null) {
            return authenticatedUser.userId();
        }
        if (principal instanceof String value) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException ignored) {
                // The username is not numeric, continue to explicit error.
            }
        }
        throw new IllegalArgumentException("Authenticated token does not include a usable userId");
    }

    public String requireUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalArgumentException("Authentication is required");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthenticatedUser authenticatedUser && authenticatedUser.username() != null && !authenticatedUser.username().isBlank()) {
            return authenticatedUser.username();
        }
        if (principal instanceof String value && !value.isBlank()) {
            return value;
        }
        throw new IllegalArgumentException("Authenticated token does not include a usable username");
    }
}
