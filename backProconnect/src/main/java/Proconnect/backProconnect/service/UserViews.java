package Proconnect.backProconnect.service;

import Proconnect.backProconnect.model.UserPublicDto;
import Proconnect.backProconnect.security.OwnershipGuard;
import org.springframework.stereotype.Component;

/**
 * Construye la vista pública de un usuario a partir de columnas sueltas (proyecciones). El email
 * solo se expone al propio usuario o a un ADMIN (regla de verify-web-security).
 */
@Component
public class UserViews {

    private final OwnershipGuard guard;

    public UserViews(OwnershipGuard guard) {
        this.guard = guard;
    }

    public UserPublicDto publicView(
            Long id,
            String name,
            String userName,
            String email,
            String role,
            String photoUrl,
            String wallPhotoUrl) {
        return new UserPublicDto(
                id,
                name,
                userName,
                guard.canSeePrivateDataOf(id) ? email : null,
                role,
                photoUrl,
                wallPhotoUrl);
    }
}
