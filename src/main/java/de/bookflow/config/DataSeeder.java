package de.bookflow.config;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import de.bookflow.entity.Booking;
import de.bookflow.entity.BookingStatus;
import de.bookflow.entity.Hotel;
import de.bookflow.entity.Role;
import de.bookflow.entity.Room;
import de.bookflow.entity.RoomType;
import de.bookflow.entity.User;
import de.bookflow.repository.BookingRepository;
import de.bookflow.repository.HotelRepository;
import de.bookflow.repository.RoomRepository;
import de.bookflow.repository.UserRepository;

@Component
@Profile("dev")
public class DataSeeder implements CommandLineRunner {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(HotelRepository hotelRepository, RoomRepository roomRepository, UserRepository userRepository,
            BookingRepository bookingRepository, PasswordEncoder passwordEncoder) {
        this.hotelRepository = hotelRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) {
            return;
        }

        // create and save the hotel
        Hotel rotanaHotel = new Hotel("Rotana", "Markt Street 2", "Germany", "Nordhorn");
        rotanaHotel = hotelRepository.save(rotanaHotel);

        // create and save the rooms
        Room room1 = new Room(rotanaHotel, 110, RoomType.SINGLE, new BigDecimal("75.00"), 1);
        Room room2 = new Room(rotanaHotel, 210, RoomType.DOUBLE, new BigDecimal("125.00"), 2);
        room1 = roomRepository.save(room1);
        room2 = roomRepository.save(room2);

        // create and save the users
        User admin1 = new User("Hamze", "Ali", "admin@bookflow.dev", passwordEncoder.encode("admin123"), Role.ADMIN);
        User customer1 = new User("Jon", "Doe", "customer1@bookflow.dev", passwordEncoder.encode("jon123"),
                Role.CUSTOMER);
        admin1 = userRepository.save(admin1);
        customer1 = userRepository.save(customer1);

        // create and save a booking
        Booking booking1 = new Booking(room1, customer1, LocalDate.of(2026, 9, 18), LocalDate.of(2026, 9, 26),
                BookingStatus.CONFIRMED, new BigDecimal("600.00"));
        booking1 = bookingRepository.save(booking1);
    }

}
