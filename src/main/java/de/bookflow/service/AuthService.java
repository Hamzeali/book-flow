package de.bookflow.service;

import java.nio.charset.StandardCharsets;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import de.bookflow.dto.RegisterRequest;
import de.bookflow.dto.UserResponse;
import de.bookflow.entity.Role;
import de.bookflow.entity.User;
import de.bookflow.exception.EmailAlreadyInUseException;
import de.bookflow.exception.InvalidPasswordException;
import de.bookflow.repository.UserRepository;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse register(RegisterRequest request) {
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new InvalidPasswordException("Password is too long");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyInUseException("Email is already registered");
        }

        User user = new User(request.firstName(), request.lastName(), request.email(),
                passwordEncoder.encode(request.password()), Role.CUSTOMER);
        user = userRepository.save(user);

        return UserResponse.from(user);
    }
}
