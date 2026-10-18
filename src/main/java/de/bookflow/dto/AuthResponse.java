package de.bookflow.dto;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn) {

}
