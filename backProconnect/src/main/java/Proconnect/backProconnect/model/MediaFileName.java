package Proconnect.backProconnect.model;

/**
 * Formato de las referencias a imágenes guardadas por el servicio de media ({@code <uuid>.<ext>}):
 * solo un nombre de archivo con extensión de imagen, sin rutas, {@code ..}, esquemas ni URLs
 * externas. Vacío o null = sin imagen.
 */
public final class MediaFileName {

    public static final String PATTERN = "^$|^[A-Za-z0-9_-]{1,100}\\.(?i:jpe?g|png|webp|gif)$";
    public static final String MESSAGE =
            "Debe ser el nombre de archivo devuelto por /upload/subirImagen (p. ej. 3f2a….png)";

    private MediaFileName() {}
}
