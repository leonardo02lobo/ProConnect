/**
 * Deterministic seed users (created by global-setup.ts through the API).
 * Credentials are test-only and exist solely in the throwaway E2E database.
 */
export const PASSWORD = process.env.E2E_USER_PASSWORD ?? 'E2e-Passw0rd!';

export const USERS = {
  alice: {
    name: 'Alice E2E',
    userName: 'alice_e2e',
    email: 'alice.e2e@proconnect.test',
    password: PASSWORD,
  },
  bob: {
    name: 'Bob E2E',
    userName: 'bob_e2e',
    email: 'bob.e2e@proconnect.test',
    password: PASSWORD,
  },
} as const;

export type SeedUserKey = keyof typeof USERS;

/** Title of the seed post (by Alice, liked and commented by Bob). */
export const SEED_POST_TITLE = 'E2E seed post';
export const SEED_COMMENT = 'E2E seed comment from Bob';

export interface SeedData {
  alice: { id: number };
  bob: { id: number };
  postId: number;
}

/**
 * Unique suffix per test: worker index + timestamp + random. Use it for every
 * entity a test creates so parallel runs (and re-runs on the same stack) never collide.
 */
export function uniqueId(workerIndex: number): string {
  return `w${workerIndex}-${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 7)}`;
}

/**
 * ADMIN created by the API at startup (BOOTSTRAP_ADMIN_* in docker-compose.e2e.yml). Registration always
 * yields USER; this account promotes throwaway users (PUT /api/admin/users/{id}/role) in admin/company-flow.
 * Never demote it: other tests log in as ADMIN.
 */
export const ADMIN = {
  email: 'admin.e2e@proconnect.test',
  password: process.env.E2E_ADMIN_PASSWORD ?? 'E2e-Admin-Passw0rd!',
} as const;
