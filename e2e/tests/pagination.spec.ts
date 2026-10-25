import { expect, postParts, test } from '../fixtures/test';

/**
 * restructure-models-performance: feed paginated by cursor ("Cargar más"), denormalized counters
 * and the 3-comment preview with the full, paginated comment list.
 */
test('feed loads more posts by cursor without repeating any', async ({ page, feed, aliceApi, uid }) => {
  // 25 own posts: more than one page (20). The oldest one is not on the first page.
  const titles: string[] = [];
  for (let i = 0; i < 25; i++) {
    const title = `Page ${uid} #${String(i).padStart(2, '0')}`;
    titles.push(title);
    await aliceApi.createPost({ title, content: 'pagination' });
  }

  // API contract: 20 items + nextCursor; the next page continues without overlap.
  const first = await aliceApi.feedPage(null, 20);
  expect(first.items).toHaveLength(20);
  expect(first.nextCursor).toBeTruthy();
  // Newest first (other workers may publish in between, so only relative order is asserted).
  const order = first.items.map((p) => p.title).filter((t) => titles.includes(t));
  expect(order).toEqual([...order].sort().reverse());
  const second = await aliceApi.feedPage(first.nextCursor, 20);
  const firstIds = new Set(first.items.map((p) => p.id));
  expect(second.items.some((p) => firstIds.has(p.id))).toBe(false);
  expect([...first.items, ...second.items].map((p) => p.title)).toEqual(expect.arrayContaining(titles));

  // UI: the oldest post appears only after "Cargar más", and no card is duplicated.
  await feed.goto();
  await expect(feed.post(titles[24])).toHaveCount(1);
  await expect(feed.post(titles[0])).toHaveCount(0);
  await expect(page.getByTestId('feed-load-more')).toBeVisible();
  await expect(await feed.reveal(titles[0])).toHaveCount(1);
  const ids = await page.getByTestId('post-card').evaluateAll((cards) => cards.map((c) => c.getAttribute('data-post-id')));
  expect(new Set(ids).size).toBe(ids.length);
});

test('counters follow like/unlike and comments (API and UI)', async ({ page, feed, aliceApi, bobApi, uid }) => {
  const title = `Counters ${uid}`;
  const { id } = await aliceApi.createPost({ title, content: 'counters' });
  const find = async () => (await bobApi.feedPage(null, 50)).items.find((p) => p.id === id)!;

  expect((await find()).likeCount).toBe(0);
  await bobApi.like(id);
  let post = await find();
  expect(post.likeCount).toBe(1);
  expect(post.userLiked).toBe(true);
  // A second like from the same user is rejected and does not change the counter.
  expect((await bobApi.ctx.post(`/api/posts/${id}/likes`)).status()).toBe(409);
  expect((await find()).likeCount).toBe(1);
  await aliceApi.like(id);
  expect((await find()).likeCount).toBe(2);
  await bobApi.unlike(id);
  post = await find();
  expect(post.likeCount).toBe(1);
  expect(post.userLiked).toBe(false);

  // 5 comments: the feed brings the 3 most recent; the full list is paginated.
  for (let i = 1; i <= 5; i++) await bobApi.comment(id, `c${i} ${uid}`);
  post = await find();
  expect(post.commentCount).toBe(5);
  expect(post.commentsPreview.map((c) => c.content)).toEqual([`c3 ${uid}`, `c4 ${uid}`, `c5 ${uid}`]);
  const all = await aliceApi.comments(id);
  expect(all.items.map((c) => c.content)).toEqual([1, 2, 3, 4, 5].map((i) => `c${i} ${uid}`));
  expect(all.nextCursor).toBeNull();

  await feed.goto();
  const p = postParts(await feed.reveal(title));
  await expect(p.likeCount).toHaveText('1');
  await expect(p.commentCount).toHaveText('5');
  await expect(p.comments).toHaveCount(3);
  await expect(p.comments.first()).toContainText(`c3 ${uid}`);
  await p.commentsExpand.click();
  await expect(p.comments).toHaveCount(5);
  await expect(p.comments.first()).toContainText(`c1 ${uid}`);
  await expect(p.commentsExpand).toHaveCount(0);
});
