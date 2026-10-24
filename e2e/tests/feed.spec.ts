import { expect, postParts, test } from '../fixtures/test';
import { SEED_COMMENT, SEED_POST_TITLE, USERS } from '../fixtures/users';

test('feed shows the seed post with author, image and counters', async ({ feed }) => {
  await feed.goto();
  const card = feed.post(SEED_POST_TITLE);
  await expect(card).toHaveCount(1);

  const p = postParts(card);
  await expect(p.author).toHaveText(USERS.alice.name);
  await expect(p.authorPhoto).toBeVisible();
  await expect(p.image).toBeVisible();
  // The image must really load through the gateway (session-protected /upload/**).
  await expect.poll(() => p.image.evaluate((img: HTMLImageElement) => img.complete && img.naturalWidth > 0)).toBe(true);

  await expect(p.likeCount).toHaveText(/^[1-9]\d*$/);
  await expect(p.commentCount).toHaveText(/^[1-9]\d*$/);
  await expect(p.comments.filter({ hasText: SEED_COMMENT })).toHaveCount(1);
});
