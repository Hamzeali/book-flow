package de.bookflow.dto;

import de.bookflow.entity.Role;
import de.bookflow.entity.User;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        Role role) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getFirstName(), user.getLastName(),
                user.getEmail(), user.getRole());
    }
}
