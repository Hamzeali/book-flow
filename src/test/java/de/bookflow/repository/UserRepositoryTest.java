package de.bookflow.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import de.bookflow.entity.Role;
import de.bookflow.entity.User;

@DataJpaTest
public class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    public void userTest() {
        User admin1 = new User("Hamze", "Ali", "admin@bookflow.dev", "admin123", Role.ADMIN);
        admin1 = userRepository.save(admin1);

        assertThat(admin1.getId()).isNotNull();

        Optional<User> foundUser = userRepository.findById(admin1.getId());

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("admin@bookflow.dev");
    }
}
