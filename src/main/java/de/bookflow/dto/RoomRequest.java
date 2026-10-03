package de.bookflow.dto;

import java.math.BigDecimal;

import de.bookflow.entity.RoomType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RoomRequest(
        @NotNull Long hotelId,
        @Positive int number,
        @NotNull RoomType type,
        @NotNull @Positive BigDecimal pricePerNight,
        @Positive int capacity) {

}
