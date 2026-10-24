// Helpers solo para código que corre en el servidor (páginas SSR y middleware).
import type { AstroGlobal } from "astro";

export const SESSION_COOKIE = "pc_session";

/** URL del gateway vista desde el servidor SSR (en docker: http://gateway:8080). */
export function serverApiUrl(): string {
    const internal = typeof process !== "undefined" ? process.env.API_INTERNAL_URL : undefined;
    return (internal || import.meta.env.PUBLIC_API_URL || "").replace(/\/+$/, "");
}

/** fetch SSR que reenvía la cookie del navegador al backend. */
export function ssrFetch(Astro: AstroGlobal, path: string, init: RequestInit = {}): Promise<Response> {
    const headers = new Headers(init.headers);
    const cookie = Astro.request.headers.get("cookie");
    if (cookie) {
        headers.set("cookie", cookie);
    }
    return fetch(`${serverApiUrl()}${path}`, { ...init, headers });
}
