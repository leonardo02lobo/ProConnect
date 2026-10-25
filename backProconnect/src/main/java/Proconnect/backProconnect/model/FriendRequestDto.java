package Proconnect.backProconnect.model;

/**
 * Solicitud o relación de amistad: {@code id} es el que se usa para aceptar, rechazar o cancelar.
 */
public record FriendRequestDto(Long id, Long requesterId, Long recipientId, String status) {}
