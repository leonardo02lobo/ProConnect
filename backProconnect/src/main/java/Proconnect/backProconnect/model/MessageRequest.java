package Proconnect.backProconnect.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Body de POST /api/messages. El emisor sale de la sesión. */
public record MessageRequest(
        @NotNull(message = "receiverId es obligatorio") @Positive Long receiverId,
        @NotBlank(message = "El mensaje no puede estar vacío") @Size(max = 255, message = "Máximo 255 caracteres") String content) {}
