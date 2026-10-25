package Proconnect.backProconnect.configuration;

import Proconnect.backProconnect.Entity.User;
import Proconnect.backProconnect.model.Role;
import Proconnect.backProconnect.repository.UserRepository;
import Proconnect.backProconnect.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Crea el primer ADMIN al arrancar si se configuran {@code BOOTSTRAP_ADMIN_EMAIL} y {@code
 * BOOTSTRAP_ADMIN_PASSWORD} y no existe ya un usuario con ese email. Sin estas variables no hace
 * nada. Nunca modifica un usuario existente (ni su rol ni su password) y nunca registra el
 * password. Es la única vía de alta de un ADMIN: el registro público siempre asigna USER.
 */
@Component
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;
    private final String name;

    public AdminBootstrap(
            UserRepository users,
            PasswordEncoder passwordEncoder,
            @Value("${proconnect.bootstrap.admin.email:}") String email,
            @Value("${proconnect.bootstrap.admin.password:}") String password,
            @Value("${proconnect.bootstrap.admin.name:Administrador}") String name) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.email = email == null ? "" : email.trim();
        this.password = password == null ? "" : password;
        this.name = name;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (email.isEmpty() || password.isEmpty()) {
            return;
        }
        if (password.length() < AuthService.MIN_PASSWORD) {
            throw new IllegalStateException(
                    "BOOTSTRAP_ADMIN_PASSWORD debe tener al menos "
                            + AuthService.MIN_PASSWORD
                            + " caracteres");
        }
        if (users.findByEmail(email).isPresent()) {
            log.info("Bootstrap de ADMIN omitido: el usuario ya existe");
            return;
        }
        String userName =
                email.substring(0, email.indexOf('@') > 0 ? email.indexOf('@') : email.length());
        users.save(
                new User(
                        null, name, userName, email, passwordEncoder.encode(password), Role.ADMIN));
        log.info("Usuario ADMIN inicial creado ({})", userName);
    }
}
