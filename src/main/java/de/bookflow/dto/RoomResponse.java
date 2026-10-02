package de.bookflow.dto;

import java.math.BigDecimal;

import de.bookflow.entity.Room;
import de.bookflow.entity.RoomType;

public record RoomResponse(Long id, Long hotelId, int number, RoomType type, BigDecimal pricePerNight, int capacity) {
    public static RoomResponse from(Room room) {
        return new RoomResponse(room.getId(), room.getHotel().getId(), room.getNumber(), room.getType(),
                room.getPricePerNight(), room.getCapacity());
    }
}
