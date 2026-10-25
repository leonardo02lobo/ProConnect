// @ts-check
import { defineConfig } from "astro/config";
import tailwindcss from "@tailwindcss/vite";
import node from "@astrojs/node";

import react from "@astrojs/react";

// URL pública del gateway (build-time): la página solo puede hablar con su origen y con el gateway.
const API_ORIGIN = (() => {
    try {
        return new URL(process.env.PUBLIC_API_URL ?? "").origin;
    } catch {
        return "";
    }
})();

// https://astro.build/config
export default defineConfig({
    // SSR: el middleware de sesión y las páginas dinámicas se resuelven en cada request.
    output: "server",
    adapter: node({ mode: "standalone" }),
    server: { port: 4321 },

    security: {
        // CSP con hashes de los scripts/estilos que Astro emite (sin 'unsafe-inline' ni 'unsafe-eval').
        // frame-ancestors no se puede poner en <meta>: lo envía src/middleware.ts como header.
        csp: {
            directives: [
                "default-src 'self'",
                /** @type {`img-src${string}`} */ (`img-src 'self' data: ${API_ORIGIN}`.trim()),
                /** @type {`connect-src${string}`} */ (`connect-src 'self' ${API_ORIGIN}`.trim()),
                "object-src 'none'",
                "base-uri 'self'",
                "form-action 'self'",
            ],
            scriptDirective: { resources: ["'self'"] },
            styleDirective: { resources: ["'self'"] },
        },
    },

    vite: {
        plugins: [tailwindcss()],
    },

    integrations: [react()],
});
