import fs from 'node:fs';
import { test as base, expect, type Locator, type Page } from '@playwright/test';
import { ApiClient } from './api';
import { AUTH_FILES, SEED_FILE } from './env';
import { uniqueId, type SeedData } from './users';

type Fixtures = {
  /** Data produced by global-setup (user ids, seed post id). */
  seed: SeedData;
  /** Unique suffix for entities created by this test. */
  uid: string;
  /** API client authenticated as Alice (the default browser user). */
  aliceApi: ApiClient;
  /** API client authenticated as Bob. */
  bobApi: ApiClient;
  feed: FeedPage;
};

export const test = base.extend<Fixtures>({
  seed: async ({}, use) => {
    await use(JSON.parse(fs.readFileSync(SEED_FILE, 'utf8')) as SeedData);
  },
  uid: async ({}, use, testInfo) => {
    await use(uniqueId(testInfo.workerIndex));
  },
  aliceApi: async ({}, use) => {
    const api = await ApiClient.create(AUTH_FILES.alice);
    await use(api);
    await api.dispose();
  },
  bobApi: async ({}, use) => {
    const api = await ApiClient.create(AUTH_FILES.bob);
    await use(api);
    await api.dispose();
  },
  feed: async ({ page }, use) => {
    await use(new FeedPage(page));
  },
});

export { expect };

/** Use inside a describe block to run without any session cookie. */
export const NO_SESSION = { cookies: [], origins: [] };

/** Minimal page object for the feed (`/`). */
export class FeedPage {
  constructor(readonly page: Page) {}

  async goto() {
    await this.page.goto('/');
    // Wait for the React island to hydrate: the composer button is rendered by React.
    await expect(this.page.getByTestId('add-post-submit')).toBeVisible();
  }

  post(title: string): Locator {
    return postCard(this.page, title);
  }

  /** The feed is paginated (20 per page): clicks "Cargar más" until the post is rendered. */
  async reveal(title: string): Promise<Locator> {
    return revealPost(this.page, title);
  }
}

export function postCard(page: Page, title: string): Locator {
  return page.getByTestId('post-card').filter({
    has: page.getByTestId('post-title').getByText(title, { exact: true }),
  });
}

/**
 * Feed and profile are paginated by cursor: click the "load more" button (`feed-load-more` or
 * `profile-load-more`) until the post appears or there are no more pages.
 */
export async function revealPost(page: Page, title: string, maxPages = 30): Promise<Locator> {
  const card = postCard(page, title);
  const more = page.getByTestId('feed-load-more').or(page.getByTestId('profile-load-more'));
  for (let i = 0; i < maxPages; i++) {
    // A page renders its posts and the "load more" button together: wait for either.
    try {
      await expect(card.or(more).first()).toBeVisible({ timeout: 10_000 });
    } catch {
      break; // no such post and no more pages: let the caller's assertion report it
    }
    if ((await card.count()) > 0) break;
    const before = await page.getByTestId('post-card').count();
    await more.click();
    await expect.poll(() => page.getByTestId('post-card').count()).toBeGreaterThan(before);
  }
  return card;
}

/** Locators scoped to one post card. */
export function postParts(card: Locator) {
  return {
    title: card.getByTestId('post-title'),
    author: card.getByTestId('post-author'),
    authorPhoto: card.getByTestId('post-author-photo'),
    image: card.getByTestId('post-image'),
    likeButton: card.getByTestId('post-like-button'),
    likeCount: card.getByTestId('post-like-count'),
    commentCount: card.getByTestId('post-comment-count'),
    commentInput: card.getByTestId('post-comment-input'),
    commentSubmit: card.getByTestId('post-comment-submit'),
    comments: card.getByTestId('comment-item'),
    commentsExpand: card.getByTestId('post-comments-expand'),
    commentsLoadMore: card.getByTestId('post-comments-load-more'),
  };
}
