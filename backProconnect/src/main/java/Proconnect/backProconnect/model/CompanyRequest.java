package Proconnect.backProconnect.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Body de POST/PUT /api/companies. El propietario sale de la sesión. */
public record CompanyRequest(
        @NotBlank(message = "El nombre es obligatorio") @Size(max = 255) String name,
        @NotBlank(message = "La descripción es obligatoria") @Size(max = 255) String description,
        @NotBlank(message = "La ubicación es obligatoria") @Size(max = 255) String location) {}
