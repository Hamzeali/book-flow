package de.bookflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import de.bookflow.entity.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long> {

}
