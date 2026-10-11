package de.bookflow.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import de.bookflow.dto.BookingRequest;
import de.bookflow.dto.BookingResponse;
import de.bookflow.entity.BookingStatus;
import de.bookflow.exception.BookingConflictException;
import de.bookflow.exception.ResourceNotFoundException;
import de.bookflow.service.BookingService;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingService bookingService;

    @Test
    void createBooking_withValidRequest_returnsCreatedBooking() throws Exception {
        // Arrange
        LocalDate checkIn = LocalDate.now().plusDays(10);
        LocalDate checkOut = checkIn.plusDays(3);

        BookingResponse response = new BookingResponse(10L, 1L, 2L, checkIn, checkOut, BookingStatus.PENDING,
                new BigDecimal("300.00"));
        when(bookingService.createBooking(any(BookingRequest.class))).thenReturn(response);

        String requestJson = """
                {
                    "roomId": 1,
                    "userId": 2,
                    "checkInDate": "%s",
                    "checkOutDate": "%s"
                }
                """.formatted(checkIn, checkOut);

        // Act / Assert
        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.checkInDate").value(checkIn.toString()));
    }

    @Test
    void createBooking_withMissingRoomId_returnsBadRequest() throws Exception {
        // Arrange
        LocalDate checkIn = LocalDate.now().plusDays(10);
        LocalDate checkOut = checkIn.plusDays(3);

        String requestJson = """
                {
                    "userId": 2,
                    "checkInDate": "%s",
                    "checkOutDate": "%s"
                }
                """.formatted(checkIn, checkOut);

        // Act / Assert
        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("roomId")));

        verify(bookingService, never()).createBooking(any());
    }

    @Test
    void createBooking_withCheckInInPast_returnsBadRequest() throws Exception {
        // Arrange
        LocalDate checkIn = LocalDate.now().minusDays(1);
        LocalDate checkOut = checkIn.plusDays(3);

        String requestJson = """
                {
                    "roomId": 1,
                    "userId": 2,
                    "checkInDate": "%s",
                    "checkOutDate": "%s"
                }
                """.formatted(checkIn, checkOut);

        // Act / Assert
        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("checkInDate")));

        verify(bookingService, never()).createBooking(any());
    }

    @Test
    void createBooking_whenRoomAlreadyBooked_returnsConflict() throws Exception {
        // Arrange
        LocalDate checkIn = LocalDate.now().plusDays(10);
        LocalDate checkOut = checkIn.plusDays(3);

        when(bookingService.createBooking(any(BookingRequest.class)))
                .thenThrow(new BookingConflictException("Room is already booked for the selected dates"));

        String requestJson = """
                {
                    "roomId": 1,
                    "userId": 2,
                    "checkInDate": "%s",
                    "checkOutDate": "%s"
                }
                """.formatted(checkIn, checkOut);

        // Act / Assert
        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Room is already booked for the selected dates"));
    }

    @Test
    void getBookingById_withUnknownId_returnsNotFound() throws Exception {
        // Arrange
        when(bookingService.getBookingById(99L))
                .thenThrow(new ResourceNotFoundException("Booking not found with id 99"));

        // Act / Assert
        mockMvc.perform(get("/api/bookings/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Booking not found with id 99"));
    }

    @Test
    void cancelBooking_withExistingBooking_returnsCancelledBooking() throws Exception {
        // Arrange
        LocalDate checkIn = LocalDate.now().plusDays(10);
        LocalDate checkOut = checkIn.plusDays(3);
        BookingResponse response = new BookingResponse(10L, 1L, 2L, checkIn, checkOut, BookingStatus.CANCELLED,
                new BigDecimal("300.00"));
        when(bookingService.cancelBooking(10L)).thenReturn(response);

        // Act / Assert
        mockMvc.perform(patch("/api/bookings/10/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
}
