import { TEST_IMAGE } from '../fixtures/api';
import { expect, postParts, test } from '../fixtures/test';
import { USERS } from '../fixtures/users';

test('create a post with an image: it appears in the feed and in the profile', async ({ page, feed, uid }) => {
  const title = `Post ${uid}`;
  const content = `Content of ${uid}`;

  await feed.goto();
  const submit = page.getByTestId('add-post-submit');
  await expect(submit).toBeDisabled();

  await page.getByTestId('add-post-title').fill(title);
  await page.getByTestId('add-post-content').fill(content);
  await page.getByTestId('add-post-image').setInputFiles(TEST_IMAGE);
  await expect(submit).toBeEnabled();
  await submit.click();

  const card = feed.post(title);
  await expect(card).toHaveCount(1);
  const p = postParts(card);
  await expect(card).toContainText(content);
  await expect(p.author).toHaveText(USERS.alice.name);
  await expect.poll(() => p.image.evaluate((img: HTMLImageElement) => img.complete && img.naturalWidth > 0)).toBe(true);

  await page.goto('/Profile');
  await expect(page.getByTestId('profile-name')).toHaveText(USERS.alice.name);
  await expect(feed.post(title)).toHaveCount(1);
});
