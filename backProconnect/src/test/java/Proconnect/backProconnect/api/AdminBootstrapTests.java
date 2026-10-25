package Proconnect.backProconnect.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import Proconnect.backProconnect.Entity.User;
import Proconnect.backProconnect.configuration.AdminBootstrap;
import Proconnect.backProconnect.repository.UserRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
class AdminBootstrapTests {

    private static final String STRONG = "Str0ng-Admin-Pass";
    private static final String STRONG_2 = "An0ther-Admin-Pass";

    @Autowired UserRepository users;
    @Autowired PasswordEncoder encoder;

    @BeforeEach
    void clean() {
        for (String email : List.of("root@example.com", "taken@example.com", "other@example.com")) {
            users.findByEmail(email).ifPresent(users::delete);
        }
    }

    private void run(String email, String password, String file, String userName) {
        new AdminBootstrap(users, encoder, email, password, file, userName, "Root")
                .run(new DefaultApplicationArguments());
    }

    private void run(String email, String password) {
        run(email, password, "", "");
    }

    private User existing(String userName, String email) {
        return users.save(
                new User(null, "Existente", userName, email, encoder.encode("x"), "USER"));
    }

    @Test
    void disabledWithoutConfiguration() {
        long before = users.count();
        run("", "");
        run("root@example.com", "");
        assertThat(users.count()).isEqualTo(before);
    }

    @Test
    void createsTheAdminOnceAndNeverOverwrites() {
        run("root@example.com", STRONG);
        User admin = users.findByEmail("root@example.com").orElseThrow();
        assertThat(admin.getRole()).isEqualTo("ADMIN");
        assertThat(admin.getUserName()).isEqualTo("root");
        assertThat(encoder.matches(STRONG, admin.getPassword())).isTrue();

        admin.setRole("USER");
        users.save(admin);
        run("root@example.com", STRONG_2);
        User again = users.findByEmail("root@example.com").orElseThrow();
        assertThat(again.getRole()).isEqualTo("USER");
        assertThat(encoder.matches(STRONG, again.getPassword())).isTrue();
    }

    @Test
    void readsThePasswordFromAFileStrippingWhitespace(@TempDir Path dir) throws Exception {
        Path secret = dir.resolve("admin_password");
        Files.writeString(secret, "  " + STRONG + "\n\n");
        run("root@example.com", "", secret.toString(), "");
        User admin = users.findByEmail("root@example.com").orElseThrow();
        assertThat(encoder.matches(STRONG, admin.getPassword())).isTrue();
    }

    @Test
    void unreadablePasswordFileFailsStartup() {
        assertThatThrownBy(() -> run("root@example.com", "", "/nonexistent/admin_password", ""))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("BOOTSTRAP_ADMIN_PASSWORD_FILE");
        assertThat(users.findByEmail("root@example.com")).isEmpty();
    }

    @Test
    void passwordAndFileTogetherIsAmbiguous(@TempDir Path dir) throws Exception {
        Path secret = dir.resolve("admin_password");
        Files.writeString(secret, STRONG);
        assertThatThrownBy(() -> run("root@example.com", STRONG, secret.toString(), ""))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("solo una");
        assertThat(users.findByEmail("root@example.com")).isEmpty();
    }

    @Test
    void rejectsWeakPasswordsWithoutLoggingThem(CapturedOutput output) {
        // Cortos, o largos con menos de 3 clases.
        for (String weak : List.of("Sh0rt!", "adminadmin12", "ALLUPPERCASE-LETTERS")) {
            assertThatThrownBy(() -> run("root@example.com", weak))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("12 caracteres")
                    .satisfies(e -> assertThat(e.getMessage()).doesNotContain(weak));
            assertThat(output.getAll()).doesNotContain(weak);
        }
        assertThat(users.findByEmail("root@example.com")).isEmpty();
        // Control positivo: 12 caracteres y 3 clases.
        run("root@example.com", "Abcdefghijk1");
        assertThat(users.findByEmail("root@example.com")).isPresent();
        assertThat(output.getAll()).doesNotContain("Abcdefghijk1");
    }

    @Test
    void derivedUserNameGetsANumericSuffixWhenTaken() {
        existing("taken", "other@example.com");
        run("taken@example.com", STRONG);
        assertThat(users.findByEmail("taken@example.com").orElseThrow().getUserName())
                .isEqualTo("taken1");
    }

    @Test
    void explicitUserNameIsUsedAndMustBeFree() {
        run("root@example.com", STRONG, "", "boss");
        assertThat(users.findByEmail("root@example.com").orElseThrow().getUserName())
                .isEqualTo("boss");

        users.findByEmail("root@example.com").ifPresent(users::delete);
        existing("boss", "other@example.com");
        assertThatThrownBy(() -> run("root@example.com", STRONG, "", "boss"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("BOOTSTRAP_ADMIN_USERNAME");
        assertThat(users.findByEmail("root@example.com")).isEmpty();
    }
}
