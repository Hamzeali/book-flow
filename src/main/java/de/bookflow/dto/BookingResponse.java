package de.bookflow.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import de.bookflow.entity.Booking;
import de.bookflow.entity.BookingStatus;

public record BookingResponse(Long id, Long roomId, Long userId, LocalDate checkInDate, LocalDate checkOutDate,
        BookingStatus status, BigDecimal totalPrice) {
    public static BookingResponse from(Booking booking) {
        return new BookingResponse(booking.getId(), booking.getRoom().getId(), booking.getUser().getId(),
                booking.getCheckInDate(), booking.getCheckOutDate(), booking.getStatus(), booking.getTotalPrice());
    }
}
