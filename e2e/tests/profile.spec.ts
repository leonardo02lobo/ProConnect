import { AUTH_FILES } from '../fixtures/env';
import { expect, revealPost, test } from '../fixtures/test';
import { SEED_POST_TITLE, USERS } from '../fixtures/users';

test('own profile shows my name and my posts', async ({ page }) => {
  await page.goto('/Profile');
  await expect(page.getByTestId('profile-name')).toHaveText(USERS.alice.name);
  await expect(await revealPost(page, SEED_POST_TITLE)).toHaveCount(1);
});

test.describe('as Bob', () => {
  test.use({ storageState: AUTH_FILES.bob });

  test('another user profile (/user/[id]) shows that user', async ({ page, seed }) => {
    await page.goto(`/user/${seed.alice.id}`);
    await expect(page.getByTestId('profile-name')).toHaveText(USERS.alice.name);
    await expect(await revealPost(page, SEED_POST_TITLE)).toHaveCount(1);
  });
});

test('clicking the author of a post opens their profile', async ({ page, feed, seed }) => {
  await feed.goto();
  await (await feed.reveal(SEED_POST_TITLE)).getByTestId('post-author').click();
  await expect(page).toHaveURL(new RegExp(`/user/${seed.alice.id}$`));
  await expect(page.getByTestId('profile-name')).toHaveText(USERS.alice.name);
});
