# ProConnect E2E (Playwright)

End-to-end suite that drives the real stack (MySQL + API + gateway + media + web) in
Chromium. **Rule: everything runs locally first, then the same scripts run in GitHub Actions.**

## Quick start

```bash
make e2e-local                 # build + start the pc-e2e stack, seed, run the suite, keep the stack up
make e2e-test ARGS="tests/like.spec.ts --headed"   # re-run against the running stack
make e2e-report                # open the HTML report
make e2e-down                  # stop the stack and drop its data
```

| Service | URL on the host |
|---|---|
| web (Astro) | http://localhost:14321 |
| gateway | http://localhost:18080 |
| api (direct) | http://localhost:18081 |
| media (direct) | http://localhost:13001 |
| mysql | localhost:13306 (db `proconnect`) |

Use `localhost`, never `127.0.0.1`: the `pc_session` cookie set by the gateway must be
visible to the web origin. Override URLs with `E2E_WEB_URL` / `E2E_API_URL`.

`e2e/scripts/run-e2e.sh` is the single runner (used by `make` and by `.github/workflows/e2e.yml`).
It generates ephemeral `E2E_JWT_SECRET` / `E2E_DB_PASSWORD`, brings the stack up with
`--wait`, runs Playwright, and on failure saves container logs with secrets redacted in
`e2e/artifacts/compose-logs.txt`.

## Layout

```
e2e/
 ├─ playwright.config.ts   baseURL from env, trace/video/screenshot retained on failure, retries=2 only in CI
 ├─ global-setup.ts        API seed (Alice, Bob, seed post with image + like + comment) → .auth/<user>.json
 ├─ fixtures/              env, users, API client, `test` with feed/uid/seed/aliceApi/bobApi fixtures
 ├─ tests/                 auth, feed, post, like, comment, profile (+ seed.spec.ts for the AI agents)
 ├─ specs/                 Markdown test plans (planner output, generator input)
 ├─ prompts/               triage.md, heal.md — shared by local scripts and workflows
 ├─ scripts/               run-e2e.sh, ai-triage.sh, ai-heal.sh, build-prompt.sh, check-guardrails.sh
 ├─ .claude/agents/        Playwright Test Agents (planner, generator, healer) for Claude Code
 └─ CLAUDE.md              selector rules and AI guardrails (read it before writing tests)
```

Data isolation: every test creates its own entities with the `uid` fixture, auth reuses
`storageState`, and the suite passes twice in a row with `--workers=4` on the same stack.

## AI workflow (local first)

All AI logic is versioned here; GitHub workflows call the same prompts via
`scripts/build-prompt.sh`, so a local run and a CI run see identical instructions.

### 1. Author tests: planner → generator → healer

The agents live in `e2e/.claude/agents/` (created by `npx playwright init-agents --loop=claude`,
patched with the project rules) and use the Playwright test MCP server from `e2e/.mcp.json`.
With the stack up, open Claude Code **inside `e2e/`**:

```bash
cd e2e && claude
> Use the playwright-test-planner agent to plan "search users from the header" and save it to specs/search.md
> Use the playwright-test-generator agent to implement specs/search.md
> Use the playwright-test-healer agent to fix tests/search.spec.ts if it fails
```

A human reviews every generated plan/test before it goes into a PR.

### 2. Triage a failure

```bash
make e2e-local            # suite fails → e2e/test-results, e2e/artifacts
make ai-triage            # = e2e/scripts/ai-triage.sh e2e/test-results
```

`claude -p` reads the failure summary, `error-context.md`, screenshots, traces and container
logs (read-only toolset) and writes `e2e/ai-triage.md` in the same format the bot posts on PRs:
cause → evidence → probable file → suggestion, classified as `APP_BUG | TEST_BROKEN | ENV | FLAKY`.

### 3. Heal broken tests

```bash
make ai-heal              # = e2e/scripts/ai-heal.sh (stack must be up)
git diff                  # review; open a PR yourself if correct
```

The healer may only touch `e2e/tests`, `e2e/fixtures` and `data-testid`/`aria-*` markup in the
frontend. `scripts/check-guardrails.sh` rejects added `skip`/`fixme`/`only`, deleted specs,
removed `expect()` calls and `waitForTimeout`. If the app is wrong, the healer reports the bug
instead of changing the assertion.

## CI (GitHub Actions)

| Workflow | Trigger | What |
|---|---|---|
| `ci.yml` | every PR, push to `main` | Maven `verify` (api, gateway), media smoke test, `astro check` + build, e2e typecheck |
| `e2e.yml` | every PR, push to `main` | buildx bake with GHA layer cache → `run-e2e.sh` → artifact `e2e-report` (report, traces, videos, logs) |
| `ai-e2e-triage.yml` | called by `e2e.yml` when it fails on a PR (or manual) | Claude reads the artifacts and comments a diagnosis on the PR; `contents: read`, never pushes |
| `ai-e2e-heal.yml` | manual (`workflow_dispatch`, input `ref`) | healer on the failing tests, guardrails, re-run, opens a separate **draft** PR |

Run them locally first:

```bash
make lint-workflows       # actionlint
make act-dry-run          # act -n over every workflow
make ci-local             # act runs ci.yml in containers
make e2e-local            # same steps as e2e.yml on the host (act + Docker-in-Docker is not reliable)
```

The AI workflows need a repository secret `ANTHROPIC_API_KEY` **or** `CLAUDE_CODE_OAUTH_TOKEN`
(`claude setup-token`). Without it they skip with a notice. `ai-e2e-heal` also needs
"Allow GitHub Actions to create and approve pull requests" enabled in the repo settings.
