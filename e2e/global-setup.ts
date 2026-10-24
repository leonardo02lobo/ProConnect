import fs from 'node:fs';
import { request, type FullConfig } from '@playwright/test';
import { ApiClient } from './fixtures/api';
import { API_URL, AUTH_DIR, AUTH_FILES, SEED_FILE, WEB_URL } from './fixtures/env';
import { SEED_COMMENT, SEED_POST_TITLE, USERS, type SeedData } from './fixtures/users';

/**
 * Seed by API (design D2): idempotent, so the suite can run many times against
 * the same stack.
 *   1. wait until gateway and web answer
 *   2. register Alice and Bob (409/400 on duplicates is fine) and log them in
 *   3. persist storageState per user in .auth/<user>.json
 *   4. ensure the seed post exists (Alice, with image), liked and commented by Bob
 */
async function waitFor(url: string, okStatuses: (s: number) => boolean, timeoutMs = 120_000) {
  const ctx = await request.newContext();
  const deadline = Date.now() + timeoutMs;
  let last = '';
  try {
    while (Date.now() < deadline) {
      try {
        const res = await ctx.get(url, { timeout: 5_000, maxRedirects: 0 });
        if (okStatuses(res.status())) return;
        last = `HTTP ${res.status()}`;
      } catch (e) {
        last = (e as Error).message.split('\n')[0];
      }
      await new Promise((r) => setTimeout(r, 1_000));
    }
  } finally {
    await ctx.dispose();
  }
  throw new Error(`Timed out waiting for ${url} (${last}). Is the E2E stack up? (make e2e-up)`);
}

export default async function globalSetup(_config: FullConfig) {
  // Any HTTP answer from the gateway auth route (even 4xx) means it is routing to the API.
  await waitFor(`${API_URL}/actuator/health`, (s) => s < 500);
  await waitFor(`${WEB_URL}/`, (s) => s < 500);

  fs.mkdirSync(AUTH_DIR, { recursive: true });

  const ids: Record<string, number> = {};
  const clients: Record<string, ApiClient> = {};
  for (const key of Object.keys(USERS) as Array<keyof typeof USERS>) {
    const user = USERS[key];
    const api = await ApiClient.create();
    const status = await api.register(user);
    if (![200, 201, 400, 409].includes(status)) {
      throw new Error(`register ${user.email} → unexpected HTTP ${status}`);
    }
    await api.login(user.email, user.password);
    const me = await api.me();
    ids[key] = me.id;
    await api.ctx.storageState({ path: AUTH_FILES[key] });
    clients[key] = api;
  }

  const { alice, bob } = clients;
  let post = await alice.findPostByTitle(ids.alice, SEED_POST_TITLE);
  if (!post) {
    const filename = await alice.uploadImage();
    await alice.createPost({ title: SEED_POST_TITLE, content: 'Post created by the E2E seed', imageUrl: filename }, ids.alice);
    post = await alice.findPostByTitle(ids.alice, SEED_POST_TITLE);
    if (!post) throw new Error('seed post was not found in the feed after creating it');
  }

  const asBob = await bob.findPostByTitle(ids.bob, SEED_POST_TITLE);
  if (asBob && !asBob.userLiked) await bob.like(post.id, ids.bob);
  if (!post.comments?.some((c) => c.content === SEED_COMMENT)) {
    await bob.comment(post.id, SEED_COMMENT, ids.bob);
  }

  const seed: SeedData = { alice: { id: ids.alice }, bob: { id: ids.bob }, postId: post.id };
  fs.writeFileSync(SEED_FILE, JSON.stringify(seed, null, 2));

  await Promise.all(Object.values(clients).map((c) => c.dispose()));
}
