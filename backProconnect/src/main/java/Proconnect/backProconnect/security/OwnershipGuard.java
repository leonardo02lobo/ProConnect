package Proconnect.backProconnect.security;

import java.util.Arrays;
import java.util.Objects;

import org.springframework.stereotype.Component;

import Proconnect.backProconnect.exception.ForbiddenException;
import Proconnect.backProconnect.exception.ResourceNotFoundException;
import Proconnect.backProconnect.model.Role;

/**
 * Autorización por propiedad y rol, centralizada para que los services no repitan lógica.
 *
 * <ul>
 * <li>Recursos públicos con mutación (posts, comentarios, empresas, empleos, usuarios, fotos, workstations):
 * el ajeno recibe 403 ({@link #assertOwnerOrAdmin}).</li>
 * <li>Recursos privados (mensajes, postulaciones): el ajeno recibe 404, igual que un id inexistente
 * ({@link #assertParticipantOrNotFound}), para no revelar su existencia.</li>
 * </ul>
 * Los checks viven en la capa de servicio, no en las rutas, para que sobrevivan a renombrados de la API.
 */
@Component
public class OwnershipGuard {

    public AuthPrincipal current() {
        return CurrentUser.principal();
    }

    public Long currentUserId() {
        return current().userId();
    }

    public boolean isAdmin() {
        return current().role() == Role.ADMIN;
    }

    public boolean hasAnyRole(Role... roles) {
        Role role = current().role();
        return Arrays.asList(roles).contains(role);
    }

    /** 403 salvo que el actor tenga alguno de los roles indicados. */
    public void requireAnyRole(Role... roles) {
        if (!hasAnyRole(roles)) {
            throw new ForbiddenException("Rol insuficiente");
        }
    }

    public boolean isSelfOrAdmin(Long ownerId) {
        return isAdmin() || (ownerId != null && Objects.equals(ownerId, currentUserId()));
    }

    /** 403 si el actor no es el propietario ni ADMIN. */
    public void assertOwnerOrAdmin(Long ownerId) {
        if (!isSelfOrAdmin(ownerId)) {
            throw new ForbiddenException("Solo el propietario puede realizar esta acción");
        }
    }

    /** 404 si el actor no es ninguno de los participantes ni ADMIN (recursos privados). */
    public void assertParticipantOrNotFound(Long... participantIds) {
        if (isAdmin()) {
            return;
        }
        Long me = currentUserId();
        for (Long id : participantIds) {
            if (id != null && id.equals(me)) {
                return;
            }
        }
        throw new ResourceNotFoundException();
    }

    /** true si el actor puede ver datos privados (p. ej. email) del usuario indicado. */
    public boolean canSeePrivateDataOf(Long userId) {
        try {
            return isSelfOrAdmin(userId);
        } catch (RuntimeException noSession) {
            return false;
        }
    }
}
