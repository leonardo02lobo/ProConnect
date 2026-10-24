# e2e/ — rules for AI agents (and humans)

This folder holds the ProConnect Playwright suite. Any agent working here (Playwright
Test Agents planner/generator/healer, `ai-triage.sh`, `ai-heal.sh`, `claude-code-action`)
MUST follow these rules.

## Stack and environment
- The stack under test is `docker-compose.e2e.yml` (compose project `pc-e2e`):
  web `http://localhost:14321`, gateway `http://localhost:18080`. Bring it up with
  `make e2e-up` from the repo root; run tests with `make e2e-local` or `npx playwright test`.
- Always use `localhost`, never `127.0.0.1` (the `pc_session` cookie is shared by host).
- The whole app requires login. Tests run as Alice by default (`.auth/alice.json`).
  Use `test.use({ storageState: AUTH_FILES.bob })` for Bob and `NO_SESSION` for anonymous.
- Seed data is created by `global-setup.ts` through the API. Never seed with SQL.

## Selectors (mandatory)
1. `getByRole` / `getByLabel` when the element has a clear accessible role or label.
2. Otherwise `getByTestId('<component>-<element>')` in kebab-case
   (`post-card`, `post-like-button`, `add-post-submit`, `login-email`, ...).
3. FORBIDDEN: CSS classes (`locator('.foo')`, Tailwind classes), `xpath=`, positional
   selectors (`nth-child`, `.nth()` to pick "the 3rd post"). Scope by content instead:
   `feed.post(title)`.
4. If a needed `data-testid` does not exist, add it to the component in
   `FrontedProConnect/src` (kebab-case) rather than using a brittle selector.

## Waiting
- Never `page.waitForTimeout`. Wait on web-first assertions (`toBeVisible`,
  `toBeEnabled`, `toHaveText`) — React islands hydrate after load.
- `expect.poll` is fine for things that are not DOM state (image `naturalWidth`, API).

## Data isolation
- Every entity a test creates uses the `uid` fixture (unique per test and worker).
- Do not mutate seed data that other tests read (seed post counters). Create your own
  post via `aliceApi` / `bobApi` when you need to like/comment.
- The suite must pass twice in a row with `--workers=4` on the same stack.

## Guardrails (non-negotiable)
- NEVER add `test.skip`, `test.fixme`, `test.only`, or delete a test to make CI green.
- NEVER weaken an assertion (e.g. `toHaveText('1')` → `toBeVisible()`, removing an
  `expect`, widening a regex, raising timeouts to hide slowness) to make a test pass.
- If a test fails because the APPLICATION is wrong (a real bug), do not touch the
  assertion: report the bug (test, request/response, suspected file) and stop.
- Only "heal" when the failure is caused by an intentional UI/markup change: update the
  locator or the flow, keep the same intent and the same strength of assertions.
- Any change proposed by AI goes through a human-reviewed PR. AI never pushes to a PR
  branch directly.

## Layout
- `specs/*.md` — test plans (planner output / generator input).
- `tests/*.spec.ts` — tests. `fixtures/` — env, users, API client, page helpers.
- `prompts/*.md` — prompts shared by local scripts and GitHub workflows (single source).
- `scripts/` — `run-e2e.sh`, `ai-triage.sh`, `ai-heal.sh`.
