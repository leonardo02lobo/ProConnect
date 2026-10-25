import path from 'node:path';

/** Browser-facing URL of the Astro frontend. */
export const WEB_URL = (process.env.E2E_WEB_URL ?? 'http://localhost:14321').replace(/\/$/, '');
/** Browser-facing URL of the gateway (= PUBLIC_API_URL of the web build). */
export const API_URL = (process.env.E2E_API_URL ?? 'http://localhost:18080').replace(/\/$/, '');

export const AUTH_DIR = path.join(__dirname, '..', '.auth');
export const AUTH_FILES = {
  alice: path.join(AUTH_DIR, 'alice.json'),
  bob: path.join(AUTH_DIR, 'bob.json'),
} as const;
export const SEED_FILE = path.join(AUTH_DIR, 'seed.json');

/** Name of the session cookie (CONTRACT.md · Auth). */
export const SESSION_COOKIE = 'pc_session';

/** Login page path. Override with E2E_LOGIN_PATH if the frontend renames it. */
export const LOGIN_PATH = process.env.E2E_LOGIN_PATH ?? '/login';
/** Regex matching any of the known login routes (pre/post cleanup naming). */
export const LOGIN_URL_RE = /\/(login|SignUp|SignIn)(\?|$|\/)/i;
