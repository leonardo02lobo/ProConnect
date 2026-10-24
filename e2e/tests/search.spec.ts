import { expect, test } from '../fixtures/test';
import { USERS } from '../fixtures/users';

test('header search: Enter lists matching users and opens their profile', async ({ page, feed, seed }) => {
  await feed.goto();
  const search = page.getByTestId('header-search');
  await search.fill('Bob E2E');
  await search.press('Enter');

  await expect(page).toHaveURL(/\/search\?q=Bob(\+|%20)E2E$/);
  const result = page.getByTestId('search-result').filter({ hasText: USERS.bob.name });
  await expect(result).toHaveCount(1);
  await result.click();
  await expect(page).toHaveURL(new RegExp(`/user/${seed.bob.id}$`));
  await expect(page.getByTestId('profile-name')).toHaveText(USERS.bob.name);
});

test('header search without matches shows an empty state', async ({ page, uid }) => {
  await page.goto(`/search?q=${encodeURIComponent(`nobody-${uid}`)}`);
  await expect(page.getByTestId('search-empty')).toBeVisible();
  await expect(page.getByTestId('search-result')).toHaveCount(0);
});
