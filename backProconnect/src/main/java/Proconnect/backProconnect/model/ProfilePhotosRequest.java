package Proconnect.backProconnect.model;

import jakarta.validation.constraints.Size;

/**
 * Body de POST /api/profile-photos (crea o reemplaza las del usuario de la sesión) y PUT /api/profile-photos/{id}
 * (en el PUT, un campo null no se modifica).
 */
public record ProfilePhotosRequest(
        @Size(max = 255, message = "Máximo 255 caracteres") String photoUrl,
        @Size(max = 255, message = "Máximo 255 caracteres") String wallPhotoUrl) {
}
