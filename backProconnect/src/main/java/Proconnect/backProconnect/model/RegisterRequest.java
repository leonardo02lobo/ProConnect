package Proconnect.backProconnect.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Body de POST /api/auth/register. Cualquier rol enviado se ignora (siempre USER). */
public record RegisterRequest(
        @NotBlank(message = "El nombre es obligatorio") @Size(max = 100, message = "Máximo 100 caracteres") String name,
        @NotBlank(message = "El userName es obligatorio") @Size(max = 50, message = "Máximo 50 caracteres") String userName,
        @NotBlank(message = "El email es obligatorio") @Email(message = "Email inválido") @Size(max = 255) String email,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        @NotBlank(message = "El password es obligatorio")
        @Size(min = 8, max = 100, message = "El password debe tener entre 8 y 100 caracteres") String password) {

    @Override
    public String toString() {
        return "RegisterRequest[userName=" + userName + "]";
    }
}
