package Proconnect.backProconnect.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Revocación de sesiones en logout: guarda el {@code jti} del JWT hasta su expiración. En memoria
 * (una instancia); el gateway no la consulta, así que en /upload/** un token revocado sigue
 * sirviendo imágenes hasta {@code exp} (riesgo aceptado y documentado en la auditoría).
 */
@Component
public class TokenDenylist {

    private final Cache<String, Instant> revoked;

    public TokenDenylist(@Value("${proconnect.jwt.ttl:PT24H}") Duration ttl) {
        this.revoked =
                Caffeine.newBuilder()
                        .expireAfterWrite(ttl.plusMinutes(1))
                        .maximumSize(1_000_000)
                        .build();
    }

    public void revoke(String jti, Instant expiresAt) {
        if (jti != null && expiresAt != null && expiresAt.isAfter(Instant.now())) {
            revoked.put(jti, expiresAt);
        }
    }

    public boolean isRevoked(String jti) {
        return jti != null && revoked.getIfPresent(jti) != null;
    }
}
