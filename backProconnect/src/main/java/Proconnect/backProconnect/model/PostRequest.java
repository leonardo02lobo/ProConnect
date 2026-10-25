package Proconnect.backProconnect.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Body de POST/PUT /api/posts. El autor sale de la sesión. */
public record PostRequest(
        @NotBlank(message = "El título es obligatorio") @Size(max = 255, message = "Máximo 255 caracteres") String title,
        @NotBlank(message = "El contenido es obligatorio") @Size(max = 255, message = "Máximo 255 caracteres") String content,
        @Size(max = 255, message = "Máximo 255 caracteres") String imageUrl) {}
