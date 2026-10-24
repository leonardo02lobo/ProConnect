package Proconnect.backProconnect.controller;

import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CookieValue;

import jakarta.servlet.http.HttpServletRequest;

import Proconnect.backProconnect.Entity.User;
import Proconnect.backProconnect.model.AuthDto;
import Proconnect.backProconnect.model.UserDto;
import Proconnect.backProconnect.model.UserPublicDto;
import Proconnect.backProconnect.security.AuthRateLimiter;
import Proconnect.backProconnect.security.ClientIpResolver;
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
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final SessionCookieFactory cookieFactory;
    private final AuthRateLimiter rateLimiter;
    private final ClientIpResolver ipResolver;

    public AuthController(AuthService authService, UserService userService, JwtService jwtService,
            SessionCookieFactory cookieFactory, AuthRateLimiter rateLimiter, ClientIpResolver ipResolver) {
        this.authService = authService;
        this.userService = userService;
        this.jwtService = jwtService;
        this.cookieFactory = cookieFactory;
        this.rateLimiter = rateLimiter;
        this.ipResolver = ipResolver;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserDto user) {
        try {
            User created = authService.register(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(userService.toSelf(created));
        } catch (AuthService.DuplicateUserException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error(HttpStatus.CONFLICT, e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(error(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthDto auth, HttpServletRequest request) {
        String email = auth == null ? null : auth.getEmail();
        String ip = ipResolver.resolve(request);
        // Cuenta bloqueada tras fallos repetidos: 429 aunque el password sea correcto.
        rateLimiter.assertNotLocked(email);
        Optional<User> user = auth == null ? Optional.empty()
                : authService.authenticate(email, auth.getPassword());
        if (user.isEmpty()) {
            int failures = rateLimiter.recordFailure(email);
            // Nunca se registra el password; el email se sanea para evitar inyección en logs.
            log.warn("Login fallido email={} ip={} fallos={}", sanitize(email), ip, failures);
            // Mensaje genérico: no revela si el email existe.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(error(HttpStatus.UNAUTHORIZED, "Credenciales inválidas"));
        }
        rateLimiter.reset(email);
        String token = jwtService.generate(user.get());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieFactory.session(token, jwtService.getTtl()).toString())
                .body(userService.toSelf(user.get()));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me() {
        UserPublicDto me = userService.getSelfById(CurrentUser.id());
        if (me == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .header(HttpHeaders.SET_COOKIE, cookieFactory.expired().toString())
                    .body(error(HttpStatus.UNAUTHORIZED, "Sesión requerida"));
        }
        return ResponseEntity.ok(me);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@CookieValue(name = "${proconnect.cookie.name:pc_session}", required = false) String token) {
        // Revoca el JWT (denylist por jti hasta exp) además de expirar la cookie.
        jwtService.revoke(token);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookieFactory.expired().toString())
                .build();
    }

    private static String sanitize(String value) {
        if (value == null) {
            return "-";
        }
        String clean = value.replaceAll("[\r\n\t]", "_");
        return clean.length() > 120 ? clean.substring(0, 120) : clean;
    }

    private static Map<String, Object> error(HttpStatus status, String message) {
        return Map.of("status", status.value(), "error", status.getReasonPhrase(), "message", message);
    }
}
