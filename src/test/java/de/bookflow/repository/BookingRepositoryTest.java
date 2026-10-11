package de.bookflow.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import de.bookflow.entity.Booking;
import de.bookflow.entity.BookingStatus;
import de.bookflow.entity.Hotel;
import de.bookflow.entity.Role;
import de.bookflow.entity.Room;
import de.bookflow.entity.RoomType;
import de.bookflow.entity.User;

@DataJpaTest
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    private Room room;
    private User user;

    @BeforeEach
    void setUp() {
        Hotel hotel = hotelRepository.save(new Hotel("Test Hotel", "Main Street 1", "Germany", "Berlin"));
        room = roomRepository.save(new Room(hotel, 101, RoomType.DOUBLE, new BigDecimal("100.00"), 2));
        user = userRepository.save(new User("Jane", "Doe", "jane@bookflow.dev", "secret", Role.CUSTOMER));

        saveBooking(room, LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 15), BookingStatus.CONFIRMED);
    }

    private void saveBooking(Room room, LocalDate checkIn, LocalDate checkOut, BookingStatus status) {
        bookingRepository.save(new Booking(room, user, checkIn, checkOut, status, new BigDecimal("500.00")));
    }

    @Test
    void existsOverlappingBooking_withSameDates_returnsTrue() {
        // Act
        boolean overlaps = bookingRepository.existsOverlappingBooking(room.getId(), LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 15));

        // Assert
        assertThat(overlaps).isTrue();
    }

    @Test
    void existsOverlappingBooking_withDatesInside_returnsTrue() {
        // Act
        boolean overlaps = bookingRepository.existsOverlappingBooking(room.getId(), LocalDate.of(2026, 11, 11),
                LocalDate.of(2026, 11, 13));

        // Assert
        assertThat(overlaps).isTrue();
    }

    @Test
    void existsOverlappingBooking_withOverlapsStart_returnsTrue() {
        // Act
        boolean overlaps = bookingRepository.existsOverlappingBooking(room.getId(), LocalDate.of(2026, 11, 8),
                LocalDate.of(2026, 11, 11));

        // Assert
        assertThat(overlaps).isTrue();
    }

    @Test
    void existsOverlappingBooking_withOverlapsEnd_returnsTrue() {
        // Act
        boolean overlaps = bookingRepository.existsOverlappingBooking(room.getId(), LocalDate.of(2026, 11, 14),
                LocalDate.of(2026, 11, 17));

        // Assert
        assertThat(overlaps).isTrue();
    }

    @Test
    void existsOverlappingBooking_withSurroundDates_returnsTrue() {
        // Act
        boolean overlaps = bookingRepository.existsOverlappingBooking(room.getId(), LocalDate.of(2026, 11, 8),
                LocalDate.of(2026, 11, 18));

        // Assert
        assertThat(overlaps).isTrue();
    }

    @Test
    void existsOverlappingBooking_withBackToBackAfter_returnsFalse() {
        // Act
        boolean overlaps = bookingRepository.existsOverlappingBooking(room.getId(), LocalDate.of(2026, 11, 15),
                LocalDate.of(2026, 11, 17));

        // Assert
        assertThat(overlaps).isFalse();
    }

    @Test
    void existsOverlappingBooking_withBackToBackBefore_returnsFalse() {
        // Act
        boolean overlaps = bookingRepository.existsOverlappingBooking(room.getId(), LocalDate.of(2026, 11, 8),
                LocalDate.of(2026, 11, 10));

        // Assert
        assertThat(overlaps).isFalse();
    }

    @Test
    void existsOverlappingBooking_withCancelledBookingOnSameDates_returnsFalse() {
        // Arrange
        Room otherRoom = roomRepository
                .save(new Room(room.getHotel(), 102, RoomType.SINGLE, new BigDecimal("80.00"), 1));
        saveBooking(otherRoom, LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 15), BookingStatus.CANCELLED);

        // Act
        boolean overlaps = bookingRepository.existsOverlappingBooking(otherRoom.getId(), LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 15));

        // Assert
        assertThat(overlaps).isFalse();
    }

    @Test
    void existsOverlappingBooking_withDifferentRoomSameDates_returnsFalse() {
        // Arrange
        Room otherRoom = roomRepository.save(
                new Room(room.getHotel(), 103, RoomType.DOUBLE, new BigDecimal("75.00"), 2));

        // Act
        boolean overlaps = bookingRepository.existsOverlappingBooking(otherRoom.getId(), LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 15));

        // Assert
        assertThat(overlaps).isFalse();
    }
}
