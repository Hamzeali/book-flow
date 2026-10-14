package de.bookflow.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.bookflow.dto.RoomRequest;
import de.bookflow.dto.RoomResponse;
import de.bookflow.entity.Hotel;
import de.bookflow.entity.Room;
import de.bookflow.entity.RoomType;
import de.bookflow.exception.ResourceNotFoundException;
import de.bookflow.repository.HotelRepository;
import de.bookflow.repository.RoomRepository;

@ExtendWith(MockitoExtension.class)
public class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private HotelRepository hotelRepository;

    @InjectMocks
    private RoomService roomService;

    private Hotel hotel;

    @BeforeEach
    void setUp() {
        hotel = new Hotel("Test Hotel", "Main Street 1", "Germany", "Berlin");
        hotel.setId(1L);
    }

    @Test
    void updateRoom_withNewHotelId_movesRoomToThatHotel() {
        // Arrange
        Room room = new Room(hotel, 101, RoomType.DOUBLE, new BigDecimal("100.00"), 2);
        room.setId(5L);

        Hotel targetHotel = new Hotel("Other Hotel", "Second Street 5", "France", "Paris");
        targetHotel.setId(2L);

        RoomRequest request = new RoomRequest(2L, 202, RoomType.SUITE, new BigDecimal("250.00"), 4);

        when(roomRepository.findById(5L)).thenReturn(Optional.of(room));
        when(hotelRepository.findById(2L)).thenReturn(Optional.of(targetHotel));
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RoomResponse response = roomService.updateRoom(5L, request);

        // Assert
        assertThat(response.id()).isEqualTo(5L);
        assertThat(response.hotelId()).isEqualTo(2L);
        assertThat(response.number()).isEqualTo(202);
        assertThat(response.type()).isEqualTo(RoomType.SUITE);
        assertThat(response.pricePerNight()).isEqualByComparingTo("250.00");
        assertThat(response.capacity()).isEqualTo(4);
    }

    @Test
    void createRoom_withExistingHotel_returnsRoomWithHotelId() {
        // Arrange
        RoomRequest request = new RoomRequest(hotel.getId(), 101, RoomType.DOUBLE, new BigDecimal("110.00"), 2);

        when(hotelRepository.findById(hotel.getId())).thenReturn(Optional.of(hotel));
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RoomResponse response = roomService.createRoom(request);

        // Assert
        assertThat(response.hotelId()).isEqualTo(1L);
        assertThat(response.number()).isEqualTo(101);
        assertThat(response.pricePerNight()).isEqualByComparingTo("110.00");
        assertThat(response.capacity()).isEqualTo(2);
        assertThat(response.type()).isEqualTo(RoomType.DOUBLE);
    }

    @Test
    void createRoom_withUnknownHotel_throwsResourceNotFoundException() {
        // Arrange
        RoomRequest request = new RoomRequest(2L, 101, RoomType.DOUBLE, new BigDecimal("110.00"), 2);
        when(hotelRepository.findById(2L)).thenReturn(Optional.empty());

        // Act / Assert
        assertThatThrownBy(() -> roomService.createRoom(request)).isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Hotel not found with id 2");

        verify(roomRepository, never()).save(any());
    }

    @Test
    void deleteRoom_withExistingRoom_deletesIt() {
        // Arrange
        Room room = new Room(hotel, 101, RoomType.DOUBLE, new BigDecimal("100.00"), 2);
        room.setId(5L);

        when(roomRepository.findById(5L)).thenReturn(Optional.of(room));

        // Act
        roomService.deleteRoom(5L);

        // Assert
        verify(roomRepository).delete(room);
    }
}
