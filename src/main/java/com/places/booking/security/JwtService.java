package com.places.booking.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public Set<String> extractRoles(String token) {
        Object rolesObj = parseClaims(token).get("roles");
        if (!(rolesObj instanceof Iterable<?> roles)) {
            return Collections.emptySet();
        }
        return toRoleAuthorities(roles);
    }

    public Long extractUserId(String token) {
        Claims claims = parseClaims(token);
        Object userIdObj = claims.get("userId");

        if (userIdObj instanceof Number number) {
            return number.longValue();
        }
        if (userIdObj instanceof String value && !value.isBlank()) {
            return parseLongQuietly(value.trim());
        }
        return parseLongQuietly(claims.getSubject());
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey signingKey() {
        byte[] decoded = Decoders.BASE64.decode(secretToBase64(secret));
        return Keys.hmacShaKeyFor(decoded);
    }

    private String secretToBase64(String rawSecret) {
        return Base64.getEncoder().encodeToString(rawSecret.getBytes());
    }

    private Set<String> toRoleAuthorities(Iterable<?> roleValues) {
        return java.util.stream.StreamSupport.stream(roleValues.spliterator(), false)
                .map(Object::toString)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(value -> value.startsWith("ROLE_") ? value : "ROLE_" + value)
                .collect(Collectors.toSet());
    }

    private Long parseLongQuietly(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
