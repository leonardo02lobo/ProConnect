package Proconnect.backProconnect.service;

import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Proconnect.backProconnect.Entity.User;
import Proconnect.backProconnect.exception.ConflictException;
import Proconnect.backProconnect.model.RegisterRequest;
import Proconnect.backProconnect.model.Role;
import Proconnect.backProconnect.repository.UserRepository;

@Service
public class AuthService {

    public static final int MIN_PASSWORD = 8;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Devuelve el usuario si email y password coinciden con el hash BCrypt almacenado. */
    @Transactional(readOnly = true)
    public Optional<User> authenticate(String email, String password) {
        if (email == null || email.isBlank() || password == null) {
            return Optional.empty();
        }
        return userRepository.findByEmail(email.trim())
                .filter(user -> isBcrypt(user.getPassword()) && passwordEncoder.matches(password, user.getPassword()));
    }

    /**
     * Registra un usuario nuevo con rol USER (se ignora cualquier rol del body) y password hasheado. El formato ya
     * lo valida Bean Validation en el controller.
     *
     * @throws ConflictException si email o userName ya existen (409)
     */
    @Transactional
    public User register(RegisterRequest request) {
        String email = request.email().trim();
        String userName = request.userName().trim();
        if (userRepository.findByEmail(email).isPresent()) {
            throw new DuplicateUserException("El email ya está registrado");
        }
        if (userRepository.findByUserName(userName).isPresent()) {
            throw new DuplicateUserException("El userName ya está registrado");
        }
        User user = new User(null, request.name().trim(), userName, email, passwordEncoder.encode(request.password()),
                Role.USER.name());
        try {
            return userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateUserException("El usuario ya existe");
        }
    }

    public static boolean isBcrypt(String hash) {
        return hash != null && hash.startsWith("$2");
    }

    /** Email o userName ya registrados (409). */
    public static class DuplicateUserException extends ConflictException {
        public DuplicateUserException(String message) {
            super(message);
        }
    }
}
