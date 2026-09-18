package de.bookflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import de.bookflow.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

}
