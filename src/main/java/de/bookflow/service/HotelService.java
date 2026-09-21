package de.bookflow.service;

import java.util.List;

import org.springframework.stereotype.Service;

import de.bookflow.dto.HotelRequest;
import de.bookflow.dto.HotelResponse;
import de.bookflow.entity.Hotel;
import de.bookflow.repository.HotelRepository;

@Service
public class HotelService {
    private final HotelRepository hotelRepository;

    public HotelService(HotelRepository hotelRepository) {
        this.hotelRepository = hotelRepository;
    }

    public HotelResponse createHotel(HotelRequest request) {
        Hotel hotel = new Hotel(request.name(), request.address(), request.country(), request.city());
        hotel = hotelRepository.save(hotel);

        return HotelResponse.from(hotel);
    }

    public HotelResponse getHotelById(Long id) {
        Hotel hotel = getSingleHotelById(id);

        return HotelResponse.from(hotel);
    }

    public List<HotelResponse> getAllHotels() {
        List<Hotel> hotels = hotelRepository.findAll();

        return hotels.stream()
                .map(HotelResponse::from).toList();
    }

    public HotelResponse updateHotel(Long id, HotelRequest request) {
        Hotel hotel = getSingleHotelById(id);
        hotel.setName(request.name());
        hotel.setAddress(request.address());
        hotel.setCountry(request.country());
        hotel.setCity(request.city());

        hotel = hotelRepository.save(hotel);

        return HotelResponse.from(hotel);
    }

    public void deleteHotel(Long id) {
        Hotel hotel = getSingleHotelById(id);
        hotelRepository.delete(hotel);
    }

    private Hotel getSingleHotelById(Long id) {
        return hotelRepository.findById(id).orElseThrow(() -> new RuntimeException("Hotel not found"));
    }
}
