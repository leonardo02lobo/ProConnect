package Proconnect.backProconnect.controller;

import Proconnect.backProconnect.Entity.User;
import Proconnect.backProconnect.exception.ProblemResponses;
import Proconnect.backProconnect.model.AuthDto;
import Proconnect.backProconnect.model.RegisterRequest;
import Proconnect.backProconnect.model.UserPublicDto;
import Proconnect.backProconnect.security.AuthRateLimiter;
import Proconnect.backProconnect.security.ClientIpResolver;
import Proconnect.backProconnect.security.CurrentUser;
import Proconnect.backProconnect.security.JwtService;
import Proconnect.backProconnect.security.SessionCookieFactory;
import Proconnect.backProconnect.service.AuthService;
import Proconnect.backProconnect.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    public AuthController(
            AuthService authService,
            UserService userService,
            JwtService jwtService,
            SessionCookieFactory cookieFactory,
            AuthRateLimiter rateLimiter,
            ClientIpResolver ipResolver) {
        this.authService = authService;
        this.userService = userService;
        this.jwtService = jwtService;
        this.cookieFactory = cookieFactory;
        this.rateLimiter = rateLimiter;
        this.ipResolver = ipResolver;
    }

    /**
     * 201 con el usuario creado (rol USER); 400 con {@code errors[]} si el body no es válido; 409
     * si existe.
     */
    @PostMapping("/register")
    public ResponseEntity<UserPublicDto> register(@RequestBody @Valid RegisterRequest request) {
        User created = authService.register(request);
        return ResponseEntity.created(URI.create("/api/users/" + created.getId()))
                .body(userService.toSelf(created));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody(required = false) AuthDto auth, HttpServletRequest request) {
        String email = auth == null ? null : auth.getEmail();
        String ip = ipResolver.resolve(request);
        // Cuenta bloqueada tras fallos repetidos: 429 aunque el password sea correcto.
        rateLimiter.assertNotLocked(email);
        Optional<User> user =
                auth == null
                        ? Optional.empty()
                        : authService.authenticate(email, auth.getPassword());
        if (user.isEmpty()) {
            int failures = rateLimiter.recordFailure(email);
            // Nunca se registra el password; el email se sanea para evitar inyección en logs.
            log.warn("Login fallido email={} ip={} fallos={}", sanitize(email), ip, failures);
            // Mensaje genérico: no revela si el email existe.
            return problem(HttpStatus.UNAUTHORIZED, "Credenciales inválidas", request).build();
        }
        rateLimiter.reset(email);
        String token = jwtService.generate(user.get());
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        cookieFactory.session(token, jwtService.getTtl()).toString())
                .body(userService.toSelf(user.get()));
    }

    /** Usuario de la sesión; 401 (y cookie expirada) si el usuario del token ya no existe. */
    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest request) {
        return userService
                .findSelf(CurrentUser.id())
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(
                        () ->
                                problem(HttpStatus.UNAUTHORIZED, "Sesión requerida", request)
                                        .header(
                                                HttpHeaders.SET_COOKIE,
                                                cookieFactory.expired().toString())
                                        .build());
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @CookieValue(name = "${proconnect.cookie.name:pc_session}", required = false)
                    String token) {
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

    /**
     * Respuesta problem+json construida a mano (casos que no son excepciones, como un login
     * fallido).
     */
    private static ProblemBuilder problem(
            HttpStatus status, String detail, HttpServletRequest request) {
        return new ProblemBuilder(status, ProblemResponses.problem(status, detail, request));
    }

    private static final class ProblemBuilder {
        private final ResponseEntity.BodyBuilder builder;
        private final ProblemDetail body;

        ProblemBuilder(HttpStatus status, ProblemDetail body) {
            this.builder = ResponseEntity.status(status).contentType(ProblemResponses.PROBLEM_JSON);
            this.body = body;
        }

        ProblemBuilder header(String name, String value) {
            builder.header(name, value);
            return this;
        }

        ResponseEntity<ProblemDetail> build() {
            return builder.body(body);
        }
    }
}
