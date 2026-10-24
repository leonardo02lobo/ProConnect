package Proconnect.backProconnect.exception;

/** Recurso inexistente o privado de otro usuario (404, mismo cuerpo en ambos casos). */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException() {
        super("Recurso no encontrado");
    }
}
