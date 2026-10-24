# ProConnect — local entry points. Everything here runs locally first, then the same
# scripts run in GitHub Actions (.github/workflows/*).
COMPOSE ?= docker-compose
E2E_PROJECT ?= pc-e2e
E2E_COMPOSE = $(COMPOSE) -p $(E2E_PROJECT) -f docker-compose.e2e.yml
ACT ?= gh act
ACT_IMAGE ?= pc-act/ubuntu:act-latest
# One job at a time: matrix jobs share act's toolcache and race when installing the JDK.
ACT_FLAGS ?= --pull=false -P ubuntu-latest=$(ACT_IMAGE) --concurrent-jobs 1

.PHONY: help act-image e2e-local e2e-up e2e-down e2e-logs e2e-test e2e-report ci-local ci-local-e2e act-dry-run lint-workflows ai-triage ai-heal

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

act-image: ## Build the local act runner image (.github/act/Dockerfile)
	docker build -q -t $(ACT_IMAGE) .github/act

ci-local: act-image ## Run .github/workflows/ci.yml locally with act
	$(ACT) pull_request -W .github/workflows/ci.yml $(ACT_FLAGS)

ci-local-e2e: act-image ## Run e2e.yml with act (act mounts the host Docker socket; fallback: make e2e-local)
	$(ACT) pull_request -W .github/workflows/e2e.yml $(ACT_FLAGS) --artifact-server-path $(CURDIR)/.act-artifacts

act-dry-run: act-image ## act: parse every workflow + dry-run (-n) ci/e2e/heal/triage
	@for wf in .github/workflows/*.yml; do echo "== $$wf"; $(ACT) -W $$wf --list >/dev/null || exit 1; done
	$(ACT) pull_request -n -W .github/workflows/ci.yml $(ACT_FLAGS)
	$(ACT) pull_request -n -W .github/workflows/e2e.yml $(ACT_FLAGS)
	$(ACT) workflow_dispatch -n -W .github/workflows/ai-e2e-heal.yml --input ref=main $(ACT_FLAGS)
	$(ACT) workflow_dispatch -n -W .github/workflows/ai-e2e-triage.yml --input run_id=1 --input pr_number=1 --input head_sha=HEAD $(ACT_FLAGS)

lint-workflows: ## actionlint over .github/workflows
	actionlint -color

ai-triage: ## AI triage of the last E2E failure with Claude Code (local)
	./e2e/scripts/ai-triage.sh e2e/test-results

ai-heal: ## AI healer over the failing tests with Claude Code (local, edits files for review)
	./e2e/scripts/ai-heal.sh
