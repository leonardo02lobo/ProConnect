package Proconnect.backProconnect.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Proconnect.backProconnect.Entity.User;
import Proconnect.backProconnect.model.AuthDto;
import Proconnect.backProconnect.model.UserDto;
import Proconnect.backProconnect.model.UserPublicDto;
import Proconnect.backProconnect.security.CurrentUser;
import Proconnect.backProconnect.security.JwtService;
import Proconnect.backProconnect.security.SessionCookieFactory;
import Proconnect.backProconnect.service.AuthService;
import Proconnect.backProconnect.service.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final JwtService jwtService;
    private final SessionCookieFactory cookieFactory;

    public AuthController(AuthService authService, UserService userService, JwtService jwtService,
            SessionCookieFactory cookieFactory) {
        this.authService = authService;
        this.userService = userService;
        this.jwtService = jwtService;
        this.cookieFactory = cookieFactory;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserDto user) {
        try {
            User created = authService.register(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(userService.toPublic(created));
        } catch (AuthService.DuplicateUserException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error(HttpStatus.CONFLICT, e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(error(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthDto auth) {
        Optional<User> user = auth == null ? Optional.empty()
                : authService.authenticate(auth.getEmail(), auth.getPassword());
        if (user.isEmpty()) {
            // Mensaje genérico: no revela si el email existe.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(error(HttpStatus.UNAUTHORIZED, "Credenciales inválidas"));
        }
        String token = jwtService.generate(user.get());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieFactory.session(token, jwtService.getTtl()).toString())
                .body(userService.toPublic(user.get()));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me() {
        UserPublicDto me = userService.getPublicById(CurrentUser.id());
        if (me == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .header(HttpHeaders.SET_COOKIE, cookieFactory.expired().toString())
                    .body(error(HttpStatus.UNAUTHORIZED, "Sesión requerida"));
        }
        return ResponseEntity.ok(me);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookieFactory.expired().toString())
                .build();
    }

    private static Map<String, Object> error(HttpStatus status, String message) {
        return Map.of("status", status.value(), "error", status.getReasonPhrase(), "message", message);
    }
}
