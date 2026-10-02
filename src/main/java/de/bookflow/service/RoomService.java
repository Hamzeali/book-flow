package de.bookflow.service;

import java.util.List;

import org.springframework.stereotype.Service;

import de.bookflow.dto.RoomRequest;
import de.bookflow.dto.RoomResponse;
import de.bookflow.entity.Hotel;
import de.bookflow.entity.Room;
import de.bookflow.exception.ResourceNotFoundException;
import de.bookflow.repository.HotelRepository;
import de.bookflow.repository.RoomRepository;

@Service
public class RoomService {
    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;

    public RoomService(RoomRepository roomRepository, HotelRepository hotelRepository) {
        this.roomRepository = roomRepository;
        this.hotelRepository = hotelRepository;
    }

    public RoomResponse createRoom(RoomRequest request) {
        Hotel hotel = getSingleHotelById(request.hotelId());

        Room room = new Room(hotel, request.number(), request.type(), request.pricePerNight(), request.capacity());
        room = roomRepository.save(room);

        return RoomResponse.from(room);
    }

    public RoomResponse getRoomById(Long id) {
        Room room = getSingleRoomById(id);

        return RoomResponse.from(room);
    }

    public List<RoomResponse> getAllRooms() {
        List<Room> rooms = roomRepository.findAll();

        return rooms.stream()
                .map(RoomResponse::from).toList();
    }

    public RoomResponse updateRoom(Long id, RoomRequest request) {
        Room room = getSingleRoomById(id);
        Hotel hotel = getSingleHotelById(request.hotelId());

        room.setHotel(hotel);
        room.setNumber(request.number());
        room.setPricePerNight(request.pricePerNight());
        room.setType(request.type());
        room.setCapacity(request.capacity());

        room = roomRepository.save(room);

        return RoomResponse.from(room);
    }

    public void deleteRoom(Long id) {
        Room room = getSingleRoomById(id);
        roomRepository.delete(room);
    }

    private Room getSingleRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id " + id));
    }

    private Hotel getSingleHotelById(Long hotelId) {
        return hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id " + hotelId));
    }
}
