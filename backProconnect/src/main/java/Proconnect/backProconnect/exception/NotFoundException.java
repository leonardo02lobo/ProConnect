package Proconnect.backProconnect.exception;

/** Recurso inexistente (o privado de otro usuario, para no revelar su existencia): 404. */
public class NotFoundException extends RuntimeException {
    public NotFoundException() {
        super("Recurso no encontrado");
    }

    public NotFoundException(String message) {
        super(message);
    }
}
