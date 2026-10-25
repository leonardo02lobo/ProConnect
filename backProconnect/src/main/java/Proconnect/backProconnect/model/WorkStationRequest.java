package Proconnect.backProconnect.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Body de POST/PUT /api/workstations. El propietario sale de la sesión. */
public record WorkStationRequest(
        @NotBlank(message = "El puesto es obligatorio") @Size(max = 255) String workStation) {}
