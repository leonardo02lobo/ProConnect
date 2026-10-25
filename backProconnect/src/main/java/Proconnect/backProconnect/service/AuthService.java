package Proconnect.backProconnect.service;

import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import Proconnect.backProconnect.Entity.User;
import Proconnect.backProconnect.model.Role;
import Proconnect.backProconnect.model.UserDto;
import Proconnect.backProconnect.repository.UserRepository;

@Service
public class AuthService {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    public static final int MIN_PASSWORD = 8;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Devuelve el usuario si email y password coinciden con el hash BCrypt almacenado. */
    public Optional<User> authenticate(String email, String password) {
        if (email == null || email.isBlank() || password == null) {
            return Optional.empty();
        }
        return userRepository.findByEmail(email.trim())
                .filter(user -> isBcrypt(user.getPassword()) && passwordEncoder.matches(password, user.getPassword()));
    }

    /**
     * Registra un usuario nuevo con rol USER (se ignora cualquier rol del body) y password hasheado.
     *
     * @throws IllegalArgumentException si los datos no son válidos (400)
     * @throws DuplicateUserException   si email, userName o name ya existen (409)
     */
    public User register(UserDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Body requerido");
        }
        String email = dto.getEmail() == null ? "" : dto.getEmail().trim();
        String userName = dto.getUserName() == null ? "" : dto.getUserName().trim();
        String name = dto.getName() == null ? "" : dto.getName().trim();
        if (!EMAIL.matcher(email).matches()) {
            throw new IllegalArgumentException("Email inválido");
        }
        if (userName.isEmpty() || name.isEmpty()) {
            throw new IllegalArgumentException("name y userName son obligatorios");
        }
        if (dto.getPassword() == null || dto.getPassword().length() < MIN_PASSWORD) {
            throw new IllegalArgumentException("El password debe tener al menos " + MIN_PASSWORD + " caracteres");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new DuplicateUserException("El email ya está registrado");
        }
        if (userRepository.findByUserName(userName).isPresent()) {
            throw new DuplicateUserException("El userName ya está registrado");
        }
        User user = new User(null, name, userName, email, passwordEncoder.encode(dto.getPassword()),
                Role.USER.name());
        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateUserException("El usuario ya existe");
        }
    }

    public static boolean isBcrypt(String hash) {
        return hash != null && hash.startsWith("$2");
    }

    public static class DuplicateUserException extends RuntimeException {
        public DuplicateUserException(String message) {
            super(message);
        }
    }
}
