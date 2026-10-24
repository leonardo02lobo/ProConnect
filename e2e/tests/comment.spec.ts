import { expect, postParts, test } from '../fixtures/test';
import { USERS } from '../fixtures/users';

test('comment on a post and see it with its author', async ({ page, feed, bobApi, seed, uid }) => {
  // Bob owns the post, Alice (browser user) comments on it.
  const title = `Comment ${uid}`;
  await bobApi.createPost({ title, content: 'post to comment' }, seed.bob.id);
  const text = `Nice post ${uid}`;

  await feed.goto();
  const p = postParts(feed.post(title));
  await expect(p.commentCount).toHaveText('0');
  await expect(p.commentSubmit).toBeDisabled();

  await p.commentInput.fill(text);
  await p.commentSubmit.click();

  const comment = p.comments.filter({ hasText: text });
  await expect(comment).toHaveCount(1);
  await expect(comment.getByTestId('comment-author')).toHaveText(USERS.alice.name);
  await expect(p.commentCount).toHaveText('1');

  await page.reload();
  await expect(p.comments.filter({ hasText: text })).toHaveCount(1);
});
