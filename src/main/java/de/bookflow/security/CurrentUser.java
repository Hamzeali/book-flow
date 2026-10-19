package de.bookflow.security;

import org.springframework.security.oauth2.jwt.Jwt;

import de.bookflow.entity.Role;

public record CurrentUser(Long id, Role role) {

    public static CurrentUser from(Jwt jwt) {
        return new CurrentUser(Long.valueOf(jwt.getSubject()), Role.valueOf(jwt.getClaimAsString("role")));
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
}
