package de.bookflow.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

public record BookingRequest(
                @NotNull Long roomId,
                @NotNull @FutureOrPresent LocalDate checkInDate,
                @NotNull LocalDate checkOutDate) {
}
