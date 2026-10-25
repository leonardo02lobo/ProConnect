package Proconnect.backProconnect.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Body de {@code PUT /api/admin/users/{id}/role}: USER, COMPANY_ADMIN o ADMIN. */
public record RoleChangeRequest(
        @NotBlank(message = "El rol es obligatorio") @Size(max = 32) String role) {}
