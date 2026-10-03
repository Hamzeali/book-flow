package de.bookflow.dto;

import jakarta.validation.constraints.NotBlank;

public record HotelRequest(
        @NotBlank String name,
        @NotBlank String address,
        @NotBlank String country,
        @NotBlank String city) {
}
