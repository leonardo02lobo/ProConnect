package Proconnect.backProconnect.security;

import Proconnect.backProconnect.model.Role;
import Proconnect.backProconnect.repository.UserRepository;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Rol efectivo de un usuario, leído de la BD (no del claim del JWT) para que un cambio de rol tenga
 * efecto inmediato en las sesiones abiertas. Cache local corta (por defecto 30 s) que se invalida
 * tras el commit de cada cambio de rol. Con varias instancias, la invalidación es solo local: bajar
 * el TTL o usar una cache distribuida.
 */
@Component
public class RoleResolver {

    private final UserRepository users;
    private final Cache<Long, Role> cache;

    public RoleResolver(
            UserRepository users,
            @Value("${proconnect.security.role-cache-ttl:PT30S}") Duration ttl) {
        this.users = users;
        this.cache = Caffeine.newBuilder().expireAfterWrite(ttl).maximumSize(100_000).build();
    }

    /** Rol vigente, o vacío si el usuario ya no existe (no se cachea la ausencia). */
    public Optional<Role> currentRole(Long userId) {
        if (userId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(cache.get(userId, id -> users.findRoleById(id).orElse(null)));
    }

    public void invalidate(Long userId) {
        if (userId != null) {
            cache.invalidate(userId);
        }
    }

    public void invalidateAll() {
        cache.invalidateAll();
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onRoleChanged(RoleChangedEvent event) {
        invalidate(event.userId());
    }
}
