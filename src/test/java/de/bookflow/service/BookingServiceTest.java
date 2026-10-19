package de.bookflow.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import de.bookflow.dto.BookingRequest;
import de.bookflow.dto.BookingResponse;
import de.bookflow.entity.Booking;
import de.bookflow.entity.BookingStatus;
import de.bookflow.entity.Hotel;
import de.bookflow.entity.Role;
import de.bookflow.entity.Room;
import de.bookflow.entity.RoomType;
import de.bookflow.entity.User;
import de.bookflow.exception.BookingConflictException;
import de.bookflow.exception.InvalidBookingException;
import de.bookflow.exception.ResourceNotFoundException;
import de.bookflow.repository.BookingRepository;
import de.bookflow.repository.RoomRepository;
import de.bookflow.repository.UserRepository;
import de.bookflow.security.CurrentUser;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {
    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BookingService bookingService;

    private Room room;
    private User user;

    private final CurrentUser customer = new CurrentUser(2L, Role.CUSTOMER);
    private final CurrentUser admin = new CurrentUser(1L, Role.ADMIN);

    @BeforeEach
    void setUp() {
        Hotel hotel = new Hotel("Test Hotel", "Main Street 1", "Germany", "Berlin");
        room = new Room(hotel, 101, RoomType.DOUBLE, new BigDecimal("100.00"), 2);
        user = new User("Jane", "Doe", "jane@bookflow.dev", "secret", Role.CUSTOMER);
        user.setId(2L);
    }

    @Test
    void createBooking_withValidRequest_savesPendingBookingWithCalculatedPrice() {
        // Arrange
        LocalDate checkIn = LocalDate.of(2026, 11, 10);
        LocalDate checkOut = LocalDate.of(2026, 11, 13);
        BookingRequest request = new BookingRequest(1L, checkIn, checkOut);

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(bookingRepository.existsOverlappingBooking(1L, checkIn, checkOut)).thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        BookingResponse response = bookingService.createBooking(request, customer);

        // Assert
        assertThat(response.status()).isEqualTo(BookingStatus.PENDING);
        assertThat(response.totalPrice()).isEqualByComparingTo("300.00");
        assertThat(response.checkInDate()).isEqualTo(checkIn);
        assertThat(response.checkOutDate()).isEqualTo(checkOut);
    }

    @Test
    void createBooking_withCheckoutBeforeCheckIn_throwsInvalidBookingException() {
        // Arrange
        LocalDate checkIn = LocalDate.of(2026, 11, 13);
        LocalDate checkOut = LocalDate.of(2026, 11, 10);
        BookingRequest request = new BookingRequest(1L, checkIn, checkOut);

        // Act / Assert
        assertThatThrownBy(() -> bookingService.createBooking(request, customer))
                .isInstanceOf(InvalidBookingException.class)
                .hasMessage("Check-out date must be after check-in date");

        // Assert
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_withSameCheckInAndCheckOut_throwsInvalidBookingException() {
        // Arrange
        LocalDate checkIn = LocalDate.of(2026, 11, 10);
        LocalDate checkOut = LocalDate.of(2026, 11, 10);
        BookingRequest request = new BookingRequest(1L, checkIn, checkOut);

        // Act / Assert
        assertThatThrownBy(() -> bookingService.createBooking(request, customer))
                .isInstanceOf(InvalidBookingException.class)
                .hasMessage("Check-out date must be after check-in date");

        // Assert
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_withOverlappingDates_throwsBookingConflictException() {
        // Arrange
        LocalDate checkIn = LocalDate.of(2026, 11, 10);
        LocalDate checkOut = LocalDate.of(2026, 11, 13);
        BookingRequest request = new BookingRequest(1L, checkIn, checkOut);

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(bookingRepository.existsOverlappingBooking(1L, checkIn, checkOut)).thenReturn(true);

        // Act / Assert
        assertThatThrownBy(() -> bookingService.createBooking(request, customer))
                .isInstanceOf(BookingConflictException.class)
                .hasMessage("Room is already booked for the selected dates");
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_withUnknownRoom_throwsResourceNotFoundException() {
        // Arrange
        LocalDate checkIn = LocalDate.of(2026, 11, 10);
        LocalDate checkOut = LocalDate.of(2026, 11, 13);
        BookingRequest request = new BookingRequest(1L, checkIn, checkOut);

        when(roomRepository.findById(1L)).thenReturn(Optional.empty());

        // Act / Assert
        assertThatThrownBy(() -> bookingService.createBooking(request, customer))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Room not found with id 1");
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_withUnknownUser_throwsResourceNotFoundException() {
        // Arrange
        LocalDate checkIn = LocalDate.of(2026, 11, 10);
        LocalDate checkOut = LocalDate.of(2026, 11, 13);
        BookingRequest request = new BookingRequest(1L, checkIn, checkOut);

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        // Act / Assert
        assertThatThrownBy(() -> bookingService.createBooking(request, customer))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found with id 2");
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void confirmBooking_withPendingBooking_setStatusToConfirmed() {
        // Arrange
        Booking booking = bookingWithStatus(BookingStatus.PENDING);
        when(bookingRepository.findById(5L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        BookingResponse response = bookingService.confirmBooking(5L);

        // Assert
        assertThat(response.status()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void confirmBooking_withConfirmedBooking_throwsInvalidBookingException() {
        // Arrange
        Booking booking = bookingWithStatus(BookingStatus.CONFIRMED);
        when(bookingRepository.findById(5L)).thenReturn(Optional.of(booking));

        // Act / Assert
        assertThatThrownBy(() -> bookingService.confirmBooking(5L)).isInstanceOf(InvalidBookingException.class)
                .hasMessage("Only pending bookings can be confirmed!");
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void cancelBooking_withConfirmedBooking_setStatusToCancelled() {
        // Arrange
        Booking booking = bookingWithStatus(BookingStatus.CONFIRMED);
        when(bookingRepository.findById(5L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        BookingResponse response = bookingService.cancelBooking(5L, customer);

        // Assert
        assertThat(response.status()).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    void cancelBooking_withPendingBooking_setStatusToCancelled() {
        // Arrange
        Booking booking = bookingWithStatus(BookingStatus.PENDING);
        when(bookingRepository.findById(5L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        BookingResponse response = bookingService.cancelBooking(5L, customer);

        // Assert
        assertThat(response.status()).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    void cancelBooking_withCancelledBooking_throwsInvalidBookingException() {
        // Arrange
        Booking booking = bookingWithStatus(BookingStatus.CANCELLED);
        when(bookingRepository.findById(5L)).thenReturn(Optional.of(booking));

        // Act / Assert
        assertThatThrownBy(() -> bookingService.cancelBooking(5L, customer)).isInstanceOf(InvalidBookingException.class)
                .hasMessage("Booking is already cancelled");
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void getBookingById_withOtherCustomersBooking_throwsResourceNotFoundException() {
        // Arrange
        Booking booking = bookingWithStatus(BookingStatus.PENDING);
        booking.setId(5L);
        when(bookingRepository.findById(5L)).thenReturn(Optional.of(booking));

        // Act / Assert
        assertThatThrownBy(() -> bookingService.getBookingById(5L, new CurrentUser(3L, Role.CUSTOMER)))
                .isInstanceOf(ResourceNotFoundException.class).hasMessage("Booking not found with id 5");
    }

    @Test
    void getBookingById_asAdmin_returnsAnyBooking() {
        // Arrange
        Booking booking = bookingWithStatus(BookingStatus.PENDING);
        booking.setId(5L);
        when(bookingRepository.findById(5L)).thenReturn(Optional.of(booking));

        // Act
        BookingResponse response = bookingService.getBookingById(5L, admin);

        // Assert
        assertThat(response.id()).isEqualTo(5L);
    }

    @Test
    void cancelBooking_withOtherCustomersBooking_throwsResourceNotFoundException() {
        // Arrange
        Booking booking = bookingWithStatus(BookingStatus.PENDING);
        booking.setId(5L);
        when(bookingRepository.findById(5L)).thenReturn(Optional.of(booking));

        // Act / Assert
        assertThatThrownBy(() -> bookingService.cancelBooking(5L, new CurrentUser(3L, Role.CUSTOMER)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Booking not found with id 5");
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void getAllBookings_asCustomerWithOtherUserId_throwsAccessDeniedException() {
        // Act / Assert
        assertThatThrownBy(() -> bookingService.getAllBookings(3L, null, customer))
                .isInstanceOf(AccessDeniedException.class).hasMessage("You can only view your own bookings");
        verify(bookingRepository, never()).findAll();
    }

    @Test
    void getAllBookings_asCustomerWithoutFilter_returnsOnlyOwnBookings() {
        // Arrange
        Booking booking = bookingWithStatus(BookingStatus.PENDING);
        booking.setId(5L);
        when(bookingRepository.findByUserId(2L)).thenReturn(List.of(booking));

        // Act
        List<BookingResponse> responses = bookingService.getAllBookings(null, null, customer);

        // Assert
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).userId()).isEqualTo(2L);
        verify(bookingRepository, never()).findAll();
    }

    private Booking bookingWithStatus(BookingStatus status) {
        return new Booking(room, user, LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 13),
                status, new BigDecimal("300.00"));
    }
}
