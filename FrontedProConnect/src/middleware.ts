import { defineMiddleware } from "astro:middleware";
import { SESSION_COOKIE } from "./Service/server";

// Páginas accesibles sin sesión. Todo lo demás redirige a /login si no hay cookie pc_session.
// La validez del token la comprueba el backend: un 401 en SSR o en apiFetch también lleva a /login.
const PUBLIC_PATHS = new Set(["/login", "/register"]);
const STATIC_PREFIXES = ["/_astro/", "/assets/", "/favicon", "/@", "/node_modules/", "/src/"];

// Headers de seguridad de las páginas (la CSP con hashes va en <meta>, ver astro.config.mjs).
// Política adicional para respuestas que ya traen la CSP con hashes de Astro (páginas renderizadas).
const EXTRA_CSP = "frame-ancestors 'none'; object-src 'none'; base-uri 'self'; form-action 'self'";
// Política completa para el resto (404 sin página, endpoints): nada inline permitido.
const API_ORIGIN = (() => {
    try {
        return new URL(import.meta.env.PUBLIC_API_URL ?? "").origin;
    } catch {
        return "";
    }
})();
const STRICT_CSP = [
    "default-src 'self'",
    "script-src 'self'",
    "style-src 'self'",
    `img-src 'self' data: ${API_ORIGIN}`.trim(),
    `connect-src 'self' ${API_ORIGIN}`.trim(),
    "object-src 'none'",
    "base-uri 'self'",
    "form-action 'self'",
    "frame-ancestors 'none'",
].join("; ");

const SECURITY_HEADERS: Record<string, string> = {
    "X-Frame-Options": "DENY",
    "X-Content-Type-Options": "nosniff",
    "Referrer-Policy": "strict-origin-when-cross-origin",
    "Permissions-Policy": "camera=(), microphone=(), geolocation=(), payment=(), usb=()",
    "Cross-Origin-Opener-Policy": "same-origin",
    "Cross-Origin-Resource-Policy": "same-origin",
};
const HSTS = process.env.HSTS_ENABLED === "true" ? "max-age=31536000; includeSubDomains" : null;

function withSecurityHeaders(response: Response): Response {
    try {
        // La CSP de Astro (hashes) puede venir ya como header: se añade una segunda política, no se pisa.
        if (response.headers.has("Content-Security-Policy")) {
            response.headers.append("Content-Security-Policy", EXTRA_CSP);
        } else {
            response.headers.set("Content-Security-Policy", STRICT_CSP);
        }
        for (const [name, value] of Object.entries(SECURITY_HEADERS)) {
            response.headers.set(name, value);
        }
    } catch {
        return response; // headers inmutables (p. ej. redirect): se dejan como están
    }
    if (HSTS) {
        response.headers.set("Strict-Transport-Security", HSTS);
    }
    return response;
}

export const onRequest = defineMiddleware(async (context, next) => {
    const { pathname } = context.url;
    if (PUBLIC_PATHS.has(pathname) || STATIC_PREFIXES.some((p) => pathname.startsWith(p))) {
        return withSecurityHeaders(await next());
    }
    if (!context.cookies.get(SESSION_COOKIE)?.value) {
        return withSecurityHeaders(context.redirect("/login"));
    }
    return withSecurityHeaders(await next());
});
