package Proconnect.backProconnect.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Body de POST /api/posts/{postId}/comments. Post y autor salen de la ruta y la sesión. */
public record CommentRequest(
        @NotBlank(message = "El comentario no puede estar vacío") @Size(max = 255, message = "Máximo 255 caracteres") String content) {
}
