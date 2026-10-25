package Proconnect.backProconnect.model;

import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Slice;

/**
 * Página de un listado ({@code ?page=&size=}, máx. 100). {@code hasNext} sale de pedir una fila más
 * (Slice): no se hace {@code COUNT(*)} de la tabla.
 */
public record PageResponse<T>(List<T> items, int page, int size, boolean hasNext) {

    public static <E, T> PageResponse<T> of(Slice<E> slice, Function<E, T> mapper) {
        return new PageResponse<>(
                slice.getContent().stream().map(mapper).toList(),
                slice.getNumber(),
                slice.getSize(),
                slice.hasNext());
    }
}
