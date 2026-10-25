package Proconnect.backProconnect.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import Proconnect.backProconnect.Entity.User;
import Proconnect.backProconnect.configuration.AdminBootstrap;
import Proconnect.backProconnect.model.Role;
import Proconnect.backProconnect.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
class AdminBootstrapTests {

    @Autowired UserRepository users;
    @Autowired PasswordEncoder encoder;

    @BeforeEach
    void clean() {
        users.findByEmail("root@example.com").ifPresent(users::delete);
    }

    private void run(String email, String password) {
        new AdminBootstrap(users, encoder, email, password, "Root")
                .run(new DefaultApplicationArguments());
    }

    @Test
    void disabledWithoutConfiguration() {
        long before = users.count();
        run("", "");
        assertThat(users.count()).isEqualTo(before);
    }

    @Test
    void createsTheAdminOnceAndNeverOverwrites() {
        run("root@example.com", "first-password");
        User admin = users.findByEmail("root@example.com").orElseThrow();
        assertThat(admin.getRole()).isEqualTo(Role.ADMIN);
        assertThat(encoder.matches("first-password", admin.getPassword())).isTrue();

        admin.setRole(Role.USER);
        users.save(admin);
        run("root@example.com", "second-password");
        User again = users.findByEmail("root@example.com").orElseThrow();
        assertThat(again.getRole()).isEqualTo(Role.USER);
        assertThat(encoder.matches("first-password", again.getPassword())).isTrue();
    }

    @Test
    void rejectsShortPasswords() {
        assertThatThrownBy(() -> run("root@example.com", "short"))
                .isInstanceOf(IllegalStateException.class);
    }
}
