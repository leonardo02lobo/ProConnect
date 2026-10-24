# ProConnect — local entry points. Everything here runs locally first, then the same
# scripts run in GitHub Actions (.github/workflows/*).
COMPOSE ?= docker-compose
E2E_PROJECT ?= pc-e2e
E2E_COMPOSE = $(COMPOSE) -p $(E2E_PROJECT) -f docker-compose.e2e.yml
ACT ?= gh act
ACT_FLAGS ?= --container-architecture linux/amd64 -P ubuntu-latest=catthehacker/ubuntu:act-latest

.PHONY: help e2e-local e2e-up e2e-down e2e-logs e2e-test e2e-report ci-local ci-local-e2e act-dry-run lint-workflows ai-triage ai-heal

help: ## Show targets
	@grep -E '^[a-zA-Z0-9_-]+:.*?## ' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-16s\033[0m %s\n", $$1, $$2}'

e2e-local: ## Build + start the E2E stack, run Playwright, print report path (ARGS="..." for playwright)
	E2E_KEEP_STACK=$${E2E_KEEP_STACK:-1} ./e2e/scripts/run-e2e.sh $(ARGS)

e2e-up: ## Start the E2E stack only (web :14321, gateway :18080)
	$(E2E_COMPOSE) up -d --build --wait

e2e-down: ## Stop the E2E stack and drop its volumes
	$(E2E_COMPOSE) down -v --remove-orphans

e2e-logs: ## Follow E2E stack logs
	$(E2E_COMPOSE) logs -f --tail=200

e2e-test: ## Run Playwright against an already running stack (ARGS="...")
	E2E_SKIP_UP=1 ./e2e/scripts/run-e2e.sh $(ARGS)

e2e-report: ## Open the last HTML report
	cd e2e && npx playwright show-report

ci-local: ## Run .github/workflows/ci.yml locally with act
	$(ACT) pull_request -W .github/workflows/ci.yml $(ACT_FLAGS)

ci-local-e2e: ## Try e2e.yml with act (needs Docker socket; fallback: make e2e-local)
	$(ACT) pull_request -W .github/workflows/e2e.yml $(ACT_FLAGS) --container-options "-v /var/run/docker.sock:/var/run/docker.sock"

act-dry-run: ## Dry-run every workflow with act (syntax + job graph)
	@for wf in .github/workflows/*.yml; do echo "== $$wf"; $(ACT) -n -W $$wf $(ACT_FLAGS) --list >/dev/null && echo ok || exit 1; done

lint-workflows: ## actionlint over .github/workflows
	actionlint -color

ai-triage: ## AI triage of the last E2E failure with Claude Code (local)
	./e2e/scripts/ai-triage.sh e2e/test-results

ai-heal: ## AI healer over the failing tests with Claude Code (local, edits files for review)
	./e2e/scripts/ai-heal.sh
