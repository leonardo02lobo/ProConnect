package Proconnect.backProconnect.exception;

import java.util.List;

/** Datos de entrada inválidos detectados en la capa de servicio: 400 con {@code errors[]}. */
public class ValidationException extends RuntimeException {

    private final List<FieldError> errors;

    public ValidationException(String message) {
        this(message, List.of());
    }

    public ValidationException(String field, String message) {
        this(message, List.of(new FieldError(field, message)));
    }

    public ValidationException(String message, List<FieldError> errors) {
        super(message);
        this.errors = List.copyOf(errors);
    }

    public List<FieldError> getErrors() {
        return errors;
    }

    /** Un campo inválido y su mensaje. */
    public record FieldError(String field, String message) {
    }
}
