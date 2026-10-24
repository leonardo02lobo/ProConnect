// @ts-check
import { defineConfig } from 'astro/config';
import tailwindcss from '@tailwindcss/vite';
import node from '@astrojs/node';

import react from '@astrojs/react';

// https://astro.build/config
export default defineConfig({
  // SSR: el middleware de sesión y las páginas dinámicas se resuelven en cada request.
  output: 'server',
  adapter: node({ mode: 'standalone' }),
  server: { port: 4321 },

  vite: {
      plugins: [tailwindcss()],
  },

  integrations: [react()]
});
