package Proconnect.backProconnect.exception;

/** El estado actual del recurso impide la operación (duplicado, ya resuelto...): 409. */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
