package de.bookflow.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import de.bookflow.entity.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("""
            SELECT COUNT(b) > 0 FROM Booking b
            WHERE b.room.id = :roomId
                AND b.status <> de.bookflow.entity.BookingStatus.CANCELLED
                AND b.checkInDate < :checkOutDate
                AND b.checkOutDate > :checkInDate
            """)
    boolean existsOverlappingBooking(@Param("roomId") Long roomId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate);
}
