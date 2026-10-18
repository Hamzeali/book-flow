package de.bookflow.service;

import java.nio.charset.StandardCharsets;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import de.bookflow.dto.AuthResponse;
import de.bookflow.dto.LoginRequest;
import de.bookflow.dto.RegisterRequest;
import de.bookflow.dto.UserResponse;
import de.bookflow.entity.Role;
import de.bookflow.entity.User;
import de.bookflow.exception.EmailAlreadyInUseException;
import de.bookflow.exception.InvalidCredentialsException;
import de.bookflow.exception.InvalidPasswordException;
import de.bookflow.repository.UserRepository;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
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

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        return new AuthResponse(tokenService.createToken(user), "Bearer", tokenService.getExpirationSeconds());
    }
}
