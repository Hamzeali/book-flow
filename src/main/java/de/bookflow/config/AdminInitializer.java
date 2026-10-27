package de.bookflow.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import de.bookflow.entity.Role;
import de.bookflow.entity.User;
import de.bookflow.repository.UserRepository;

@Component
public class AdminInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public AdminInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder,
            @Value("${bookflow.admin.email:}") String adminEmail,
            @Value("${bookflow.admin.password:}") String adminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (adminEmail.isBlank() || adminPassword.isBlank()) {
            log.info("No admin credentials configured - skipping admin creation");
            return;
        }
        if (userRepository.existsByEmail(adminEmail)) {
            return;
        }
        userRepository.save(new User("Admin", "BookFlow", adminEmail,
                passwordEncoder.encode(adminPassword), Role.ADMIN));
    }
}
