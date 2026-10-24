import { defineMiddleware } from "astro:middleware";
import { SESSION_COOKIE } from "./Service/server";

// Páginas accesibles sin sesión. Todo lo demás redirige a /login si no hay cookie pc_session.
// La validez del token la comprueba el backend: un 401 en SSR o en apiFetch también lleva a /login.
const PUBLIC_PATHS = new Set(["/login", "/register", "/SignUp", "/SignIn"]);
const STATIC_PREFIXES = ["/_astro/", "/assets/", "/favicon", "/@", "/node_modules/", "/src/"];

export const onRequest = defineMiddleware((context, next) => {
    const { pathname } = context.url;
    if (PUBLIC_PATHS.has(pathname) || STATIC_PREFIXES.some((p) => pathname.startsWith(p))) {
        return next();
    }
    if (!context.cookies.get(SESSION_COOKIE)?.value) {
        return context.redirect("/login");
    }
    return next();
});
