// spec: specs/register-validation.md
// seed: tests/seed.spec.ts

import { ApiClient } from '../fixtures/api';
import { API_URL, SESSION_COOKIE } from '../fixtures/env';
import { NO_SESSION, expect, test } from '../fixtures/test';
import { USERS } from '../fixtures/users';

const VALID_PASSWORD = 'Passw0rd!123';
const SHORT_PASSWORD = '1234567';

test.describe('registration validation', () => {
  test.use({ storageState: NO_SESSION });

  test('Duplicate email is rejected', async ({ page, uid }) => {
    const name = `Dup ${uid}`;
    const userName = `dup_${uid}`.replace(/-/g, '_');

    // 1. Go to /register, fill the four fields with Alice's email and submit
    await page.goto('/register');
    await expect(page.getByTestId('register-submit')).toBeVisible();
    await page.getByTestId('register-name').fill(name);
    await page.getByTestId('register-username').fill(userName);
    await page.getByTestId('register-email').fill(USERS.alice.email);
    await page.getByTestId('register-password').fill(VALID_PASSWORD);

    const responsePromise = page.waitForResponse(
      (r) => r.url().endsWith('/api/auth/register') && r.request().method() === 'POST',
    );
    await page.getByTestId('register-submit').click();
    const response = await responsePromise;

    expect(response.status()).toBe(409);
    await expect(page.getByTestId('register-error')).toBeVisible();
    await expect(page.getByTestId('register-error')).toHaveText('El email ya está registrado');
    await expect(page).toHaveURL(/\/register$/);
    await expect(page.getByTestId('add-post-submit')).toBeHidden();

    // 2. Verify no account was overwritten and Alice's account is intact
    const api = await ApiClient.create();
    try {
      const attempted = await api.loginRaw(USERS.alice.email, VALID_PASSWORD);
      expect(attempted.status()).toBe(401);

      const user = await api.login(USERS.alice.email, USERS.alice.password);
      expect(user.email).toBe(USERS.alice.email);
    } finally {
      await api.dispose();
    }
  });

  test('Password shorter than 8 characters is rejected', async ({ page, uid }) => {
    const email = `short_${uid}@proconnect.test`;
    const registerRequests: string[] = [];
    page.on('request', (req) => {
      if (req.url().endsWith('/api/auth/register') && req.method() === 'POST') {
        registerRequests.push(req.url());
      }
    });

    // 1. Go to /register, fill the four fields with a 7-char password, submit
    await page.goto('/register');
    await expect(page.getByTestId('register-submit')).toBeVisible();
    await page.getByTestId('register-name').fill(`Short ${uid}`);
    await page.getByTestId('register-username').fill(`short_${uid}`.replace(/-/g, '_'));
    await page.getByTestId('register-email').fill(email);
    const passwordInput = page.getByTestId('register-password');
    await passwordInput.fill(SHORT_PASSWORD);
    await page.getByTestId('register-submit').click();

    await expect
      .poll(() => passwordInput.evaluate((el: HTMLInputElement) => el.validity.tooShort))
      .toBe(true);
    await expect(page).toHaveURL(/\/register$/);
    await expect(page.getByTestId('register-error')).toBeHidden();
    expect(registerRequests).toHaveLength(0);

    // 2. Attempt API login with the short-password credentials
    const api = await ApiClient.create();
    try {
      expect((await api.loginRaw(email, SHORT_PASSWORD)).status()).toBe(401);

      // 3. Backend defense: direct register with the short password is rejected
      const directEmail = `direct_${uid}@proconnect.test`;
      const status = await api.register({
        name: `Direct ${uid}`,
        userName: `direct_${uid}`.replace(/-/g, '_'),
        email: directEmail,
        password: SHORT_PASSWORD,
      });
      expect(status).toBe(400);
      expect((await api.loginRaw(directEmail, SHORT_PASSWORD)).status()).toBe(401);
    } finally {
      await api.dispose();
    }
  });

  test('Successful registration with unique data', async ({ page, context, uid }) => {
    const name = `Ok ${uid}`;

    // 1. Go to /register, fill the four fields with unique data, submit
    await page.goto('/register');
    await expect(page.getByTestId('register-submit')).toBeVisible();
    await page.getByTestId('register-name').fill(name);
    await page.getByTestId('register-username').fill(`ok_${uid}`.replace(/-/g, '_'));
    await page.getByTestId('register-email').fill(`ok-${uid}@proconnect.test`);
    await page.getByTestId('register-password').fill(VALID_PASSWORD);
    await page.getByTestId('register-submit').click();

    await expect(page).toHaveURL(/\/$/);
    await expect(page.getByTestId('add-post-submit')).toBeVisible();
    await expect(page.getByTestId('header-user-menu')).toContainText(name);

    const cookies = await context.cookies(API_URL);
    expect(cookies.find((c) => c.name === SESSION_COOKIE), 'pc_session cookie must be set').toBeDefined();
  });
});
