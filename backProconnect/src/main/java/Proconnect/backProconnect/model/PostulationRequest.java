package Proconnect.backProconnect.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Body de POST/PUT /api/postulations. El postulante sale de la sesión. */
public record PostulationRequest(@NotNull(message = "jobId es obligatorio") @Positive Long jobId) {
}
