package Proconnect.backProconnect.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Body de POST /api/friends/requests: destinatario de la solicitud (el emisor sale de la sesión).
 */
public record FriendRequestCreate(
        @NotNull(message = "userId es obligatorio") @Positive Long userId) {}
