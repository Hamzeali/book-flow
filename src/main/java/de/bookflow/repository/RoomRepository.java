package de.bookflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import de.bookflow.entity.Room;

public interface RoomRepository extends JpaRepository<Room, Long> {

}
