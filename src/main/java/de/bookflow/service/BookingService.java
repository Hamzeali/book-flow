package de.bookflow.service;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;

import de.bookflow.dto.BookingRequest;
import de.bookflow.dto.BookingResponse;
import de.bookflow.entity.Booking;
import de.bookflow.entity.BookingStatus;
import de.bookflow.entity.Room;
import de.bookflow.entity.User;
import de.bookflow.exception.BookingConflictException;
import de.bookflow.exception.InvalidBookingException;
import de.bookflow.exception.ResourceNotFoundException;
import de.bookflow.repository.BookingRepository;
import de.bookflow.repository.RoomRepository;
import de.bookflow.repository.UserRepository;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public BookingService(BookingRepository bookingRepository, RoomRepository roomRepository,
            UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }

    public BookingResponse createBooking(BookingRequest request) {
        // validate the dates
        if (!request.checkOutDate().isAfter(request.checkInDate())) {
            throw new InvalidBookingException("Check-out date must be after check-in date");
        }

        // load the room and the user
        Room room = getSingleRoomById(request.roomId());
        User user = getSingleUserById(request.userId());

        // check for overlaps
        boolean bookingOverlap = bookingRepository.existsOverlappingBooking(request.roomId(), request.checkInDate(),
                request.checkOutDate());
        if (bookingOverlap) {
            throw new BookingConflictException("Room is already booked for the selected dates");
        }

        // calculate the price
        long nights = ChronoUnit.DAYS.between(request.checkInDate(), request.checkOutDate());
        BigDecimal totalPrice = room.getPricePerNight().multiply(BigDecimal.valueOf(nights));

        // build and save the booking
        Booking booking = new Booking(room, user, request.checkInDate(), request.checkOutDate(), BookingStatus.PENDING,
                totalPrice);
        Booking saved = bookingRepository.save(booking);

        return BookingResponse.from(saved);
    }

    public BookingResponse getBookingById(Long id) {
        Booking booking = getSingleBookingById(id);

        return BookingResponse.from(booking);
    }

    public List<BookingResponse> getAllBookings(Long userId, Long roomId) {
        List<Booking> bookings;

        if (userId != null && roomId != null) {
            bookings = bookingRepository.findByUserIdAndRoomId(userId, roomId);
        } else if (userId != null) {
            bookings = bookingRepository.findByUserId(userId);
        } else if (roomId != null) {
            bookings = bookingRepository.findByRoomId(roomId);
        } else {
            bookings = bookingRepository.findAll();
        }

        return bookings.stream()
                .map(BookingResponse::from).toList();
    }

    public BookingResponse confirmBooking(Long id) {
        Booking booking = getSingleBookingById(id);
        BookingStatus status = booking.getStatus();
        if (status != BookingStatus.PENDING) {
            throw new InvalidBookingException("Only pending bookings can be confirmed!");
        }

        booking.setStatus(BookingStatus.CONFIRMED);

        Booking savedBooking = bookingRepository.save(booking);

        return BookingResponse.from(savedBooking);
    }

    public BookingResponse cancelBooking(Long id) {
        Booking booking = getSingleBookingById(id);
        BookingStatus status = booking.getStatus();

        if (status == BookingStatus.CANCELLED) {
            throw new InvalidBookingException("Booking is already cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);

        Booking savedBooking = bookingRepository.save(booking);

        return BookingResponse.from(savedBooking);
    }

    private Room getSingleRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id " + id));
    }

    private User getSingleUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
    }

    private Booking getSingleBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id " + id));
    }
}
