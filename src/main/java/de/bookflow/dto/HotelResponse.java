package de.bookflow.dto;

import de.bookflow.entity.Hotel;

public record HotelResponse(Long id, String name, String address, String country, String city) {
    public static HotelResponse from(Hotel hotel) {
        return new HotelResponse(hotel.getId(), hotel.getName(), hotel.getAddress(), hotel.getCountry(),
                hotel.getCity());
    }
}
