package Proconnect.backProconnect.security;

import Proconnect.backProconnect.exception.TooManyRequestsException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Protección contra fuerza bruta en /api/auth/**, en memoria (una sola instancia; si se escala
 * horizontalmente hay que moverlo a Redis).
 *
 * <ul>
 *   <li>Por IP: {@code RATE_LIMIT_AUTH_PER_MINUTE} peticiones/minuto (10 por defecto) con Bucket4j.
 *   <li>Por cuenta: tras {@code RATE_LIMIT_MAX_FAILURES} fallos consecutivos (5) la cuenta queda
 *       bloqueada {@code RATE_LIMIT_LOCKOUT} (15 min), aunque luego llegue el password correcto.
 * </ul>
 */
@Component
public class AuthRateLimiter {

    private final int perMinute;
    private final int maxFailures;
    private final Duration lockout;
    private final Cache<String, Bucket> ipBuckets;
    private final Cache<String, Attempts> attempts;

    public AuthRateLimiter(
            @Value("${proconnect.ratelimit.auth-per-minute:10}") int perMinute,
            @Value("${proconnect.ratelimit.max-failures:5}") int maxFailures,
            @Value("${proconnect.ratelimit.lockout:PT15M}") Duration lockout) {
        this.perMinute = perMinute;
        this.maxFailures = maxFailures;
        this.lockout = lockout;
        this.ipBuckets =
                Caffeine.newBuilder()
                        .expireAfterAccess(10, TimeUnit.MINUTES)
                        .maximumSize(100_000)
                        .build();
        this.attempts =
                Caffeine.newBuilder()
                        .expireAfterWrite(lockout.plusMinutes(1))
                        .maximumSize(100_000)
                        .build();
    }

    /** Consume un token del bucket de la IP o lanza 429 con el tiempo de espera. */
    public void consumeIp(String ip) {
        Bucket bucket =
                ipBuckets.get(
                        ip == null ? "unknown" : ip,
                        k ->
                                Bucket.builder()
                                        .addLimit(
                                                Bandwidth.builder()
                                                        .capacity(perMinute)
                                                        .refillIntervally(
                                                                perMinute, Duration.ofMinutes(1))
                                                        .build())
                                        .build());
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (!probe.isConsumed()) {
            throw new TooManyRequestsException(
                    (long) Math.ceil(probe.getNanosToWaitForRefill() / 1_000_000_000.0));
        }
    }

    /** 429 si la cuenta está bloqueada por fallos previos. */
    public void assertNotLocked(String email) {
        Attempts a = attempts.getIfPresent(key(email));
        if (a != null && a.lockedUntil != null) {
            long seconds = Duration.between(Instant.now(), a.lockedUntil).toSeconds();
            if (seconds > 0) {
                throw new TooManyRequestsException(seconds);
            }
            attempts.invalidate(key(email));
        }
    }

    /** Registra un fallo; al llegar al máximo bloquea la cuenta. Devuelve los fallos acumulados. */
    public int recordFailure(String email) {
        Attempts a =
                attempts.asMap()
                        .compute(
                                key(email),
                                (k, prev) -> {
                                    Attempts next = prev == null ? new Attempts() : prev;
                                    next.failures++;
                                    if (next.failures >= maxFailures) {
                                        next.lockedUntil = Instant.now().plus(lockout);
                                    }
                                    return next;
                                });
        return a.failures;
    }

    public void reset(String email) {
        attempts.invalidate(key(email));
    }

    private static String key(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private static final class Attempts {
        int failures;
        Instant lockedUntil;
    }
}
