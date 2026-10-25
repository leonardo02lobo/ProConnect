// ESLint (flat config) del servicio de media. Prettier se encarga del formato.
import js from '@eslint/js'
import tseslint from 'typescript-eslint'
import prettier from 'eslint-config-prettier'
import globals from 'globals'

export default tseslint.config(
  { ignores: ['node_modules/', 'upload/'] },
  js.configs.recommended,
  ...tseslint.configs.recommended,
  {
    files: ['**/*.{ts,mjs}'],
    languageOptions: { globals: globals.node },
    // Servicio de servidor: el log por consola es su salida (stdout de Docker).
    rules: { 'no-console': 'off' },
  },
  prettier,
)
