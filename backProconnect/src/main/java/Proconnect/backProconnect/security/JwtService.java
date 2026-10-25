package Proconnect.backProconnect.security;

import Proconnect.backProconnect.Entity.User;
import Proconnect.backProconnect.model.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Firma y valida los JWT de sesión (HS256). */
@Service
public class JwtService {

    private final SecretKey key;
    private final Duration ttl;
    private final TokenDenylist denylist;

    /** Solo para tests unitarios de configuración (sin denylist). */
    public JwtService(String secret, Duration ttl) {
        this(secret, ttl, new TokenDenylist(ttl));
    }

    @org.springframework.beans.factory.annotation.Autowired
    public JwtService(
            @Value("${proconnect.jwt.secret:}") String secret,
            @Value("${proconnect.jwt.ttl:PT24H}") Duration ttl,
            TokenDenylist denylist) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "JWT_SECRET no está definido. Defínelo (>= 32 bytes) antes de arrancar la API.");
        }
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET debe tener al menos 32 bytes (tiene " + bytes.length + ")");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.ttl = ttl;
        this.denylist = denylist;
    }

    public Duration getTtl() {
        return ttl;
    }

    public String generate(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(user.getId()))
                .claim("role", Role.fromString(user.getRole()).name())
                .claim("name", user.getName())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /** Devuelve el principal si la firma y la expiración son válidas. */
    public Optional<AuthPrincipal> parse(String token) {
        try {
            Claims claims =
                    Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            if (denylist.isRevoked(claims.getId())) {
                return Optional.empty();
            }
            Long userId = Long.valueOf(claims.getSubject());
            Role role = Role.fromString(claims.get("role", String.class));
            return Optional.of(new AuthPrincipal(userId, role, claims.get("name", String.class)));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /** Revoca el token (logout) hasta su expiración. Ignora tokens inválidos. */
    public void revoke(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        try {
            Claims claims =
                    Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            if (claims.getExpiration() != null) {
                denylist.revoke(claims.getId(), claims.getExpiration().toInstant());
            }
        } catch (JwtException | IllegalArgumentException e) {
            // token inválido o expirado: nada que revocar
        }
    }
}
