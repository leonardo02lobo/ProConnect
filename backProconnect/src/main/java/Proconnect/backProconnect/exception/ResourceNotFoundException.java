package Proconnect.backProconnect.exception;

/**
 * Alias histórico de {@link NotFoundException} (introducido por verify-web-security). Se mantiene
 * para que las ramas que aún lo usan compilen; el código nuevo usa {@link NotFoundException}.
 */
public class ResourceNotFoundException extends NotFoundException {
    public ResourceNotFoundException() {
        super();
    }
}
