import { expect, postParts, test } from '../fixtures/test';
import { USERS } from '../fixtures/users';

test('comment on a post and see it with its author', async ({ page, feed, bobApi, seed, uid }) => {
  // Bob owns the post, Alice (browser user) comments on it.
  const title = `Comment ${uid}`;
  await bobApi.createPost({ title, content: 'post to comment' });
  const text = `Nice post ${uid}`;

  await feed.goto();
  // Paginated feed: parallel tests may publish newer posts, so look the post up by cursor.
  const p = postParts(await feed.reveal(title));
  await expect(p.commentCount).toHaveText('0');
  await expect(p.commentSubmit).toBeDisabled();

  await page.evaluate(() => ((window as unknown as { __noReload: boolean }).__noReload = true));
  await p.commentInput.fill(text);
  await p.commentSubmit.click();

  const comment = p.comments.filter({ hasText: text });
  await expect(comment).toHaveCount(1);
  await expect(comment.getByTestId('comment-author')).toHaveText(USERS.alice.name);
  await expect(p.commentCount).toHaveText('1');
  await expect(comment).toHaveAttribute('aria-busy', 'false');
  await expect(p.commentInput).toHaveValue('');
  // Optimistic update without a full page reload (spec code-quality-standards).
  expect(await page.evaluate(() => (window as unknown as { __noReload?: boolean }).__noReload)).toBe(true);

  await page.reload();
  await feed.reveal(title);
  await expect(p.comments.filter({ hasText: text })).toHaveCount(1);
});
