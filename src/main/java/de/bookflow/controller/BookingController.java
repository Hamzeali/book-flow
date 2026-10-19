package de.bookflow.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.security.oauth2.jwt.Jwt;

import de.bookflow.dto.BookingRequest;
import de.bookflow.dto.BookingResponse;
import de.bookflow.security.CurrentUser;
import de.bookflow.service.BookingService;
import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        BookingResponse response = bookingService.createBooking(request, CurrentUser.from(jwt));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public BookingResponse getBookingById(@PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        return bookingService.getBookingById(id, CurrentUser.from(jwt));
    }

    @GetMapping
    public List<BookingResponse> getAllBookings(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long roomId,
            @AuthenticationPrincipal Jwt jwt) {
        return bookingService.getAllBookings(userId, roomId, CurrentUser.from(jwt));
    }

    @PatchMapping("/{id}/confirm")
    public BookingResponse confirmBooking(@PathVariable Long id) {
        return bookingService.confirmBooking(id);
    }

    @PatchMapping("/{id}/cancel")
    public BookingResponse cancelBooking(@PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        return bookingService.cancelBooking(id, CurrentUser.from(jwt));
    }
}
