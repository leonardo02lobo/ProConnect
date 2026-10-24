package Proconnect.backProconnect.exception;

/** El actor está autenticado pero no puede operar sobre el recurso (403). */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
