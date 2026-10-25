package Proconnect.backProconnect.configuration;

import Proconnect.backProconnect.Entity.User;
import Proconnect.backProconnect.model.Role;
import Proconnect.backProconnect.repository.UserRepository;
import Proconnect.backProconnect.security.PasswordPolicy;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Crea el primer ADMIN al arrancar si se configuran {@code BOOTSTRAP_ADMIN_EMAIL} y un password
 * ({@code BOOTSTRAP_ADMIN_PASSWORD} o {@code BOOTSTRAP_ADMIN_PASSWORD_FILE}, p. ej. un Docker
 * secret) y no existe ya un usuario con ese email. Sin configuración no hace nada.
 *
 * <ul>
 *   <li>Nunca modifica un usuario existente (ni su rol ni su password) y nunca registra el
 *       password.
 *   <li>Definir a la vez el password y el archivo es un error de arranque (configuración ambigua).
 *   <li>El password debe cumplir {@link PasswordPolicy#bootstrap()} (≥ 12, 3 de 4 clases).
 *   <li>userName: {@code BOOTSTRAP_ADMIN_USERNAME} si se da (si ya existe, error de arranque); si
 *       no, la parte local del email con sufijo numérico (admin, admin1, admin2...) si está
 *       ocupado.
 * </ul>
 *
 * Tras el primer arranque conviene retirar las variables {@code BOOTSTRAP_ADMIN_*}. Los demás roles
 * se asignan después con {@code PUT /api/admin/users/{id}/role}.
 */
@Component
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);
    private static final int MAX_USERNAME_SUFFIX = 10_000;

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;
    private final String passwordFile;
    private final String userName;
    private final String name;

    public AdminBootstrap(
            UserRepository users,
            PasswordEncoder passwordEncoder,
            @Value("${proconnect.bootstrap.admin.email:}") String email,
            @Value("${proconnect.bootstrap.admin.password:}") String password,
            @Value("${proconnect.bootstrap.admin.password-file:}") String passwordFile,
            @Value("${proconnect.bootstrap.admin.username:}") String userName,
            @Value("${proconnect.bootstrap.admin.name:Administrador}") String name) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.email = trim(email);
        this.password = password == null ? "" : password;
        this.passwordFile = trim(passwordFile);
        this.userName = trim(userName);
        this.name = name == null || name.isBlank() ? "Administrador" : name.trim();
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!password.isEmpty() && !passwordFile.isEmpty()) {
            throw new IllegalStateException(
                    "Define solo una de BOOTSTRAP_ADMIN_PASSWORD o BOOTSTRAP_ADMIN_PASSWORD_FILE");
        }
        if (email.isEmpty() || (password.isEmpty() && passwordFile.isEmpty())) {
            if (!email.isEmpty()) {
                log.warn("BOOTSTRAP_ADMIN_EMAIL definido sin password: bootstrap desactivado");
            }
            return;
        }
        String secret = passwordFile.isEmpty() ? password : readPasswordFile(passwordFile);
        PasswordPolicy policy = PasswordPolicy.bootstrap();
        if (!policy.accepts(secret)) {
            throw new IllegalStateException(
                    "El password del ADMIN inicial no cumple la política: " + policy.describe());
        }
        if (users.findByEmail(email).isPresent()) {
            log.info("Bootstrap de ADMIN omitido: el usuario ya existe");
            return;
        }
        String chosenUserName = resolveUserName();
        users.save(
                new User(
                        null,
                        name,
                        chosenUserName,
                        email,
                        passwordEncoder.encode(secret),
                        Role.ADMIN));
        log.info(
                "Usuario ADMIN inicial creado ({}). Retira las variables BOOTSTRAP_ADMIN_* tras"
                        + " este primer arranque.",
                chosenUserName);
    }

    private String resolveUserName() {
        if (!userName.isEmpty()) {
            if (users.existsByUserName(userName)) {
                throw new IllegalStateException(
                        "BOOTSTRAP_ADMIN_USERNAME ya pertenece a otro usuario: elige otro");
            }
            return userName;
        }
        int at = email.indexOf('@');
        String base = at > 0 ? email.substring(0, at) : email;
        if (!users.existsByUserName(base)) {
            return base;
        }
        for (int i = 1; i <= MAX_USERNAME_SUFFIX; i++) {
            String candidate = base + i;
            if (!users.existsByUserName(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException(
                "No se encontró un userName libre para el ADMIN inicial: define"
                        + " BOOTSTRAP_ADMIN_USERNAME");
    }

    /** Contenido del archivo sin espacios ni saltos de línea al principio o al final. */
    private static String readPasswordFile(String file) {
        try {
            return Files.readString(Path.of(file)).strip();
        } catch (IOException | RuntimeException e) {
            // Sin el contenido ni la causa en el mensaje: solo la ruta configurada.
            throw new IllegalStateException(
                    "No se pudo leer BOOTSTRAP_ADMIN_PASSWORD_FILE (" + file + ")");
        }
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
