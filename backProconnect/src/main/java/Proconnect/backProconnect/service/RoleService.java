package Proconnect.backProconnect.service;

import Proconnect.backProconnect.Entity.RoleChange;
import Proconnect.backProconnect.Entity.User;
import Proconnect.backProconnect.exception.ConflictException;
import Proconnect.backProconnect.exception.ForbiddenException;
import Proconnect.backProconnect.exception.NotFoundException;
import Proconnect.backProconnect.exception.ValidationException;
import Proconnect.backProconnect.model.AdminUserDto;
import Proconnect.backProconnect.model.AdminUserPage;
import Proconnect.backProconnect.model.Role;
import Proconnect.backProconnect.repository.RoleChangeRepository;
import Proconnect.backProconnect.repository.UserRepository;
import Proconnect.backProconnect.security.OwnershipGuard;
import Proconnect.backProconnect.security.RoleChangedEvent;
import Proconnect.backProconnect.security.RoleResolver;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Administración de roles (solo ADMIN). Salvaguardas: un ADMIN no puede quitarse su propio rol y
 * siempre queda al menos un ADMIN; ambas se comprueban con los ADMIN bloqueados ({@code SELECT ...
 * FOR UPDATE}) dentro de la transacción, así que dos degradaciones concurrentes no pueden dejar el
 * sistema sin ADMIN. Cada cambio efectivo queda en {@code role_changes} y se invalida la cache de
 * {@link RoleResolver} tras el commit.
 */
@Service
public class RoleService {

    public static final int MAX_PAGE_SIZE = 100;
    public static final int DEFAULT_PAGE_SIZE = 20;

    private static final Logger log = LoggerFactory.getLogger(RoleService.class);
    private final UserRepository users;
    private final RoleChangeRepository roleChanges;
    private final OwnershipGuard guard;
    private final RoleResolver roleResolver;
    private final ApplicationEventPublisher events;

    public RoleService(
            UserRepository users,
            RoleChangeRepository roleChanges,
            OwnershipGuard guard,
            RoleResolver roleResolver,
            ApplicationEventPublisher events) {
        this.users = users;
        this.roleChanges = roleChanges;
        this.guard = guard;
        this.roleResolver = roleResolver;
        this.events = events;
    }

    /** Usuarios con su rol, filtrados por nombre/userName/email; página de 1 a 100 elementos. */
    @Transactional(readOnly = true)
    public AdminUserPage listUsers(String query, int page, int size) {
        guard.requireAnyRole(Role.ADMIN);
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Page<User> result =
                users.adminSearch(
                        likePattern(query), PageRequest.of(safePage, safeSize, Sort.by("id")));
        return new AdminUserPage(
                result.getContent().stream().map(RoleService::toDto).toList(),
                safePage,
                safeSize,
                result.getTotalElements(),
                result.getTotalPages());
    }

    /**
     * Asigna {@code requestedRole} al usuario {@code targetId}.
     *
     * <ul>
     *   <li>403 si el actor no es ADMIN (también si dejó de serlo y su rol estaba en cache).
     *   <li>400 si el rol no es USER, COMPANY_ADMIN o ADMIN; 404 si el usuario no existe.
     *   <li>409 si el actor se quita su propio rol de ADMIN o si el cambio deja 0 ADMIN.
     *   <li>200 sin auditoría si el rol ya era ese.
     * </ul>
     */
    @Transactional
    public AdminUserDto changeRole(Long targetId, String requestedRole) {
        guard.requireAnyRole(Role.ADMIN);
        Role newRole = parseRole(requestedRole);
        Long actorId = guard.currentUserId();
        if (targetId == null || targetId <= 0) {
            throw new NotFoundException("Usuario no encontrado");
        }

        // Orden fijo de bloqueo (ADMINs por id, luego el objetivo) para evitar interbloqueos.
        List<User> admins = users.findAllByRoleForUpdate(Role.ADMIN);
        User target =
                users.findByIdForUpdate(targetId)
                        .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        Role current = target.getRole();

        boolean demotesAdmin = current == Role.ADMIN && newRole != Role.ADMIN;
        if (demotesAdmin && Objects.equals(target.getId(), actorId)) {
            throw new ConflictException("Un ADMIN no puede quitarse su propio rol de ADMIN");
        }
        if (demotesAdmin
                && admins.stream().noneMatch(u -> !Objects.equals(u.getId(), target.getId()))) {
            throw new ConflictException("Debe quedar al menos un ADMIN");
        }
        if (admins.stream().noneMatch(u -> Objects.equals(u.getId(), actorId))) {
            // El rol del actor cambió después de cachearse: se refresca y se rechaza.
            roleResolver.invalidate(actorId);
            throw new ForbiddenException("Rol insuficiente");
        }
        if (current == newRole) {
            return toDto(target);
        }

        target.setRole(newRole);
        users.save(target);
        roleChanges.save(
                new RoleChange(
                        actorId, target.getId(), current.name(), newRole.name(), Instant.now()));
        events.publishEvent(new RoleChangedEvent(target.getId()));
        log.info(
                "Rol cambiado: actor={} usuario={} {} -> {}",
                actorId,
                target.getId(),
                current.name(),
                newRole.name());
        return toDto(target);
    }

    /**
     * Patrón LIKE de la búsqueda de administración: minúsculas, comodines {@code %}, {@code _} y
     * {@code \} escapados con {@code \} y envuelto en {@code %}; {@code null} si no hay filtro.
     */
    static String likePattern(String query) {
        if (query == null || query.trim().isEmpty()) {
            return null;
        }
        String escaped =
                query.trim()
                        .toLowerCase(Locale.ROOT)
                        .replace("\\", "\\\\")
                        .replace("%", "\\%")
                        .replace("_", "\\_");
        return "%" + escaped + "%";
    }

    /** Conversión estricta (Role.fromString cae a USER con valores desconocidos). */
    static Role parseRole(String value) {
        String normalized = value == null ? "" : value.trim();
        return Arrays.stream(Role.values())
                .filter(r -> r.name().equals(normalized))
                .findFirst()
                .orElseThrow(
                        () ->
                                new ValidationException(
                                        "role", "Rol no válido: usa USER, COMPANY_ADMIN o ADMIN"));
    }

    static AdminUserDto toDto(User user) {
        return new AdminUserDto(
                user.getId(),
                user.getName(),
                user.getUserName(),
                user.getEmail(),
                user.getRole().name());
    }
}
