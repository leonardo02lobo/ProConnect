/** Añade una página a una lista evitando duplicados por id (p. ej. si la primera página se recargó). */
export function appendUnique<T extends { id: number }>(current: T[], next: T[]): T[] {
    const seen = new Set(current.map((item) => item.id));
    return [...current, ...next.filter((item) => !seen.has(item.id))];
}
