package Proconnect.backProconnect.service;

import Proconnect.backProconnect.exception.ValidationException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;

/**
 * Cursor opaco {@code base64url("<epochMicros>:<id>")} sobre la clave de orden {@code (created_at,
 * id)} (design D4). Estable ante inserciones: la página siguiente empieza justo después del último
 * elemento visto, no en un offset.
 */
public record Cursor(Instant createdAt, long id) {

    public static Cursor of(Instant createdAt, long id) {
        return new Cursor(createdAt, id);
    }

    public String encode() {
        long micros = ChronoUnit.MICROS.between(Instant.EPOCH, createdAt);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString((micros + ":" + id).getBytes(StandardCharsets.US_ASCII));
    }

    /** Vacío si no hay cursor (primera página); 400 si no es un cursor válido. */
    public static Optional<Cursor> parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        try {
            String decoded =
                    new String(
                            Base64.getUrlDecoder().decode(raw.trim()), StandardCharsets.US_ASCII);
            int sep = decoded.indexOf(':');
            long micros = Long.parseLong(decoded.substring(0, sep));
            long id = Long.parseLong(decoded.substring(sep + 1));
            if (id <= 0) {
                throw new IllegalArgumentException("id");
            }
            return Optional.of(new Cursor(Instant.EPOCH.plus(micros, ChronoUnit.MICROS), id));
        } catch (RuntimeException e) {
            throw new ValidationException("cursor", "Cursor inválido");
        }
    }
}
