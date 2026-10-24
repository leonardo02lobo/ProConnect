import { defineConfig, devices } from '@playwright/test';
import { AUTH_FILES, WEB_URL } from './fixtures/env';

const isCI = !!process.env.CI;

/**
 * ProConnect E2E configuration.
 * - WEB_URL / API_URL come from env (see fixtures/env.ts); defaults target the
 *   local `pc-e2e` stack (web :14321, gateway :18080).
 * - Use `localhost` (not 127.0.0.1): the session cookie set by the gateway must be
 *   shared with the web origin.
 */
export default defineConfig({
  testDir: './tests',
  globalSetup: './global-setup.ts',
  fullyParallel: true,
  forbidOnly: isCI,
  retries: isCI ? 2 : 0,
  workers: isCI ? 2 : undefined,
  timeout: 30_000,
  expect: { timeout: 7_500 },
  reporter: isCI
    ? [['github'], ['html', { open: 'never' }], ['json', { outputFile: 'test-results/results.json' }], ['list']]
    : [['list'], ['html', { open: 'never' }], ['json', { outputFile: 'test-results/results.json' }]],
  use: {
    baseURL: WEB_URL,
    storageState: AUTH_FILES.alice,
    trace: 'retain-on-failure',
    video: 'retain-on-failure',
    screenshot: 'only-on-failure',
    actionTimeout: 10_000,
    navigationTimeout: 15_000,
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
});
