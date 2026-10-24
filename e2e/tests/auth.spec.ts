import { ApiClient } from '../fixtures/api';
import { API_URL, LOGIN_PATH, LOGIN_URL_RE, SESSION_COOKIE } from '../fixtures/env';
import { NO_SESSION, expect, test } from '../fixtures/test';
import { PASSWORD, USERS } from '../fixtures/users';

const REGISTER_PATH = process.env.E2E_REGISTER_PATH ?? '/register';

test.describe('auth without session', () => {
  test.use({ storageState: NO_SESSION });

  test('register a new account from the UI and land logged in', async ({ page, uid }) => {
    const email = `reg-${uid}@proconnect.test`;
    await page.goto(REGISTER_PATH);
    await page.getByTestId('register-name').fill(`Reg ${uid}`);
    await page.getByTestId('register-username').fill(`reg_${uid}`.replace(/-/g, '_'));
    await page.getByTestId('register-email').fill(email);
    await page.getByTestId('register-password').fill(PASSWORD);
    await page.getByTestId('register-submit').click();

    // The form navigates away (to login or feed) and the account really exists.
    await expect(page).not.toHaveURL(new RegExp(`${REGISTER_PATH}$`));
    const api = await ApiClient.create();
    try {
      const user = await api.login(email, PASSWORD);
      expect(user.email).toBe(email);
    } finally {
      await api.dispose();
    }
  });

  test('login with valid credentials opens the feed', async ({ page, context }) => {
    await page.goto(LOGIN_PATH);
    await page.getByTestId('login-email').fill(USERS.alice.email);
    await page.getByTestId('login-password').fill(USERS.alice.password);
    await page.getByTestId('login-submit').click();

    await expect(page).toHaveURL(/\/$/);
    await expect(page.getByTestId('header-user-menu')).toContainText(USERS.alice.name);
    const cookies = await context.cookies(API_URL);
    const session = cookies.find((c) => c.name === SESSION_COOKIE);
    expect(session, 'pc_session cookie must be set').toBeDefined();
    expect(session!.httpOnly).toBe(true);
  });

  test('login with a wrong password shows an error and sets no session', async ({ page, context }) => {
    await page.goto(LOGIN_PATH);
    await page.getByTestId('login-email').fill(USERS.alice.email);
    await page.getByTestId('login-password').fill('definitely-wrong-password');
    await page.getByTestId('login-submit').click();

    await expect(page.getByTestId('login-error')).toBeVisible();
    await expect(page).toHaveURL(LOGIN_URL_RE);
    const cookies = await context.cookies(API_URL);
    expect(cookies.find((c) => c.name === SESSION_COOKIE)).toBeUndefined();
  });

  for (const path of ['/', '/Profile', '/user/1']) {
    test(`visiting ${path} without session redirects to login`, async ({ page }) => {
      await page.goto(path);
      await expect(page).toHaveURL(LOGIN_URL_RE);
    });
  }

  test('API rejects requests without session with 401', async ({ playwright }) => {
    const ctx = await playwright.request.newContext({ baseURL: API_URL });
    try {
      expect((await ctx.get('/api/auth/me')).status()).toBe(401);
      expect((await ctx.get('/api/posts')).status()).toBe(401);
      expect((await ctx.post('/api/posts', { data: { title: 'x', content: 'y' } })).status()).toBe(401);
    } finally {
      await ctx.dispose();
    }
  });
});

test.describe('auth with session', () => {
  test.use({ storageState: NO_SESSION });

  test('logout clears the session and protects the feed again', async ({ page, uid }) => {
    // Dedicated user so logging out never affects the shared seed sessions.
    const email = `logout-${uid}@proconnect.test`;
    const api = await ApiClient.create();
    try {
      await api.register({ name: `Logout ${uid}`, userName: `lo_${uid}`.replace(/-/g, '_'), email, password: PASSWORD });
    } finally {
      await api.dispose();
    }

    await page.goto(LOGIN_PATH);
    await page.getByTestId('login-email').fill(email);
    await page.getByTestId('login-password').fill(PASSWORD);
    await page.getByTestId('login-submit').click();
    await expect(page).toHaveURL(/\/$/);

    await page.getByTestId('header-user-menu').click();
    await page.getByTestId('header-logout').click();
    await expect(page).toHaveURL(LOGIN_URL_RE);

    await page.goto('/');
    await expect(page).toHaveURL(LOGIN_URL_RE);
  });
});
