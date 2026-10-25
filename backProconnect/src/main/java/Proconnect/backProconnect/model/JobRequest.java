package Proconnect.backProconnect.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.Date;

/** Body de POST /api/jobs. Solo el propietario de la empresa (o ADMIN) publica. */
public record JobRequest(
        @NotNull(message = "companyId es obligatorio") @Positive Long companyId,
        @NotBlank(message = "El título es obligatorio") @Size(max = 255) String title,
        @NotBlank(message = "La descripción es obligatoria") @Size(max = 255) String description,
        @NotBlank(message = "El puesto es obligatorio") @Size(max = 255) String workStation,
        Date date) {}
