// ESLint (flat config): TypeScript, Astro y reglas de hooks de React. Prettier se encarga del formato.
import js from "@eslint/js";
import tseslint from "typescript-eslint";
import astro from "eslint-plugin-astro";
import reactHooks from "eslint-plugin-react-hooks";
import prettier from "eslint-config-prettier";
import globals from "globals";

export default tseslint.config(
    { ignores: ["dist/", ".astro/", "node_modules/"] },
    js.configs.recommended,
    ...tseslint.configs.recommended,
    ...astro.configs.recommended,
    {
        files: ["**/*.{ts,tsx,astro,mjs}"],
        languageOptions: { globals: { ...globals.browser, ...globals.node } },
        rules: {
            // Sin console.log en el frontend; warn/error se permiten para fallos reales.
            "no-console": ["error", { allow: ["warn", "error"] }],
        },
    },
    {
        files: ["**/*.tsx"],
        plugins: { "react-hooks": reactHooks },
        rules: {
            "react-hooks/rules-of-hooks": "error",
            "react-hooks/exhaustive-deps": "error",
        },
    },
    prettier,
);
