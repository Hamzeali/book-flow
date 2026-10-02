package de.bookflow.dto;

import java.math.BigDecimal;

import de.bookflow.entity.RoomType;

public record RoomRequest(Long hotelId, int number, RoomType type, BigDecimal pricePerNight, int capacity) {

}
