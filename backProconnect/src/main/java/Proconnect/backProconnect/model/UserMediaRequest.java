package Proconnect.backProconnect.model;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Body de PUT /api/users/{id}/media (sustituye a /api/profile-photos desde V3). Actualización
 * parcial: un campo {@code null} no cambia; {@code ""} lo borra. Solo nombres de archivo del
 * servicio de media.
 */
public record UserMediaRequest(
        @Size(max = 255, message = "Máximo 255 caracteres") @Pattern(regexp = MediaFileName.PATTERN, message = MediaFileName.MESSAGE) String avatarUrl,
        @Size(max = 255, message = "Máximo 255 caracteres") @Pattern(regexp = MediaFileName.PATTERN, message = MediaFileName.MESSAGE) String coverUrl) {}
