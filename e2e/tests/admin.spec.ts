import { ApiClient } from '../fixtures/api';
import { expect, test } from '../fixtures/test';
import { ADMIN, PASSWORD } from '../fixtures/users';

/**
 * add-role-management: only an ADMIN sees and uses /admin/users. The ADMIN is the one created by
 * AdminBootstrap from docker-compose.e2e.yml; every promoted user is a throwaway user of this test.
 */
test.describe('admin users page', () => {
  test('ADMIN promotes a user to COMPANY_ADMIN from the UI and it applies to the open session', async ({
    browser,
    uid,
  }) => {
    const admin = await ApiClient.create();
    const target = await ApiClient.create();
    const context = await browser.newContext();
    try {
      await admin.login(ADMIN.email, ADMIN.password);
      // The target already has a session open before the promotion.
      const user = await target.registerAndLogin(uid, 'promoted', PASSWORD);
      expect(
        (await target.ctx.post('/api/companies', { data: { name: `Before ${uid}`, description: 'x', location: 'y' } }))
          .status(),
      ).toBe(403);

      await context.addCookies((await admin.ctx.storageState()).cookies);
      const page = await context.newPage();
      await page.goto('/');
      await page.getByTestId('header-user-menu').click();
      await page.getByTestId('header-admin-link').click();
      await expect(page).toHaveURL(/\/admin\/users$/);

      await page.getByTestId('admin-users-search').fill(user.email);
      await page.getByTestId('admin-users-search-submit').click();
      const row = page.getByTestId('admin-user-row').filter({ hasText: user.email });
      await expect(row).toHaveCount(1);
      await expect(row.getByTestId('admin-user-role')).toHaveText('USER');

      await row.getByTestId('admin-user-role-select').selectOption('COMPANY_ADMIN');
      await row.getByTestId('admin-user-role-save').click();
      await expect(page.getByTestId('admin-users-status')).toContainText('COMPANY_ADMIN');
      await expect(page.getByTestId('admin-user-row').filter({ hasText: user.email }).getByTestId('admin-user-role')).toHaveText(
        'COMPANY_ADMIN',
      );

      // Same session, no re-login: the new role is effective right away.
      expect((await target.me()).role).toBe('COMPANY_ADMIN');
      const created = await target.ctx.post('/api/companies', {
        data: { name: `After ${uid}`, description: 'x', location: 'y' },
      });
      expect(created.status()).toBe(201);
    } finally {
      await context.close();
      await admin.dispose();
      await target.dispose();
    }
  });

  test('ADMIN cannot remove their own ADMIN role (409 shown in the UI)', async ({ browser }) => {
    const admin = await ApiClient.create();
    const context = await browser.newContext();
    try {
      const me = await admin.login(ADMIN.email, ADMIN.password);
      expect((await admin.setRole(me.id, 'USER')).status()).toBe(409);

      await context.addCookies((await admin.ctx.storageState()).cookies);
      const page = await context.newPage();
      await page.goto('/admin/users');
      await page.getByTestId('admin-users-search').fill(ADMIN.email);
      await page.getByTestId('admin-users-search-submit').click();
      const row = page.getByTestId('admin-user-row').filter({ hasText: ADMIN.email });
      await row.getByTestId('admin-user-role-select').selectOption('USER');
      await row.getByTestId('admin-user-role-save').click();
      await expect(page.getByTestId('admin-users-error')).toContainText('ADMIN');
      await expect(row.getByTestId('admin-user-role')).toHaveText('ADMIN');
      expect((await admin.me()).role).toBe('ADMIN');
    } finally {
      await context.close();
      await admin.dispose();
    }
  });

  test('a USER does not see the admin link and cannot open /admin/users', async ({ page, aliceApi }) => {
    await page.goto('/');
    await page.getByTestId('header-user-menu').click();
    await expect(page.getByTestId('header-profile-link')).toBeVisible();
    await expect(page.getByTestId('header-admin-link')).toHaveCount(0);

    await page.goto('/admin/users');
    await expect(page).toHaveURL(/\/$/);
    await expect(page.getByTestId('admin-users')).toHaveCount(0);

    // The API is the real barrier.
    expect((await aliceApi.ctx.get('/api/admin/users')).status()).toBe(403);
    const alice = await aliceApi.me();
    expect((await aliceApi.setRole(alice.id, 'ADMIN')).status()).toBe(403);
    expect((await aliceApi.me()).role).toBe('USER');
  });
});
