import { expect, postParts, test } from '../fixtures/test';

test('like and unlike update the counter and persist after reload', async ({ page, feed, aliceApi, seed, uid }) => {
  // Own post per test: parallel workers never touch the same like counter.
  const title = `Like ${uid}`;
  await aliceApi.createPost({ title, content: 'post to like' });

  await feed.goto();
  // Paginated feed: parallel tests may publish newer posts, so look the post up by cursor.
  const p = postParts(await feed.reveal(title));
  await expect(p.likeCount).toHaveText('0');
  await expect(p.likeButton).toHaveAttribute('aria-pressed', 'false');

  // Marker on window: it disappears if the page does a full reload.
  await page.evaluate(() => ((window as unknown as { __noReload: boolean }).__noReload = true));
  await p.likeButton.click();
  await expect(p.likeCount).toHaveText('1');
  await expect(p.likeButton).toHaveAttribute('aria-pressed', 'true');
  expect(await page.evaluate(() => (window as unknown as { __noReload?: boolean }).__noReload)).toBe(true);

  await page.reload();
  await feed.reveal(title);
  await expect(p.likeCount).toHaveText('1');
  await expect(p.likeButton).toHaveAttribute('aria-pressed', 'true');

  await p.likeButton.click();
  await expect(p.likeCount).toHaveText('0');
  await page.reload();
  await feed.reveal(title);
  await expect(p.likeCount).toHaveText('0');
  await expect(p.likeButton).toHaveAttribute('aria-pressed', 'false');
});
