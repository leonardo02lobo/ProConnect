import { expect, test } from '../fixtures/test';
import { USERS } from '../fixtures/users';

/**
 * Seed test for the Playwright Test Agents (planner/generator/healer): it leaves the
 * browser logged in as Alice on a hydrated feed. Generated tests start from here.
 */
test.describe('Test group', () => {
  test('seed', async ({ page, feed }) => {
    await feed.goto();
    await expect(page.getByTestId('header-user-menu')).toContainText(USERS.alice.name);
  });
});
