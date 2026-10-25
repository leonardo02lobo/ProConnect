#!/usr/bin/env node
// Compact Markdown summary of failed tests from Playwright's JSON report, so the AI gets
// the signal (errors, locations, attachment paths) without the whole report.
//   node summarize-results.mjs <results.json> [artifactsDir]
import fs from 'node:fs';
import path from 'node:path';

const [, , resultsPath = 'test-results/results.json', artifactsDir] = process.argv;
const strip = (s = '') => s.replace(/\u001b\[[0-9;]*m/g, '');
const out = [];

if (!fs.existsSync(resultsPath)) {
  out.push(`No Playwright JSON report at \`${resultsPath}\` (the suite may not have started: check compose logs).`);
} else {
  const report = JSON.parse(fs.readFileSync(resultsPath, 'utf8'));
  const failed = [];
  const flaky = [];
  const walk = (suite, titles) => {
    for (const s of suite.suites ?? []) walk(s, [...titles, s.title].filter(Boolean));
    for (const spec of suite.specs ?? []) {
      for (const t of spec.tests ?? []) {
        const results = t.results ?? [];
        const last = results[results.length - 1];
        const entry = { title: [...titles, spec.title].join(' › '), file: `${spec.file}:${spec.line}`, results };
        if (t.status === 'unexpected') failed.push(entry);
        else if (t.status === 'flaky') flaky.push(entry);
        void last;
      }
    }
  };
  for (const s of report.suites ?? []) walk(s, []);
  const st = report.stats ?? {};
  out.push(`## Run stats\nexpected=${st.expected ?? '?'} unexpected=${st.unexpected ?? '?'} flaky=${st.flaky ?? '?'} skipped=${st.skipped ?? '?'} duration=${Math.round((st.duration ?? 0) / 1000)}s`);
  if (report.errors?.length) {
    out.push('## Global errors (global-setup / config)');
    for (const e of report.errors) out.push('```\n' + strip(e.message ?? e.stack ?? JSON.stringify(e)).slice(0, 3000) + '\n```');
  }
  out.push(`## Failed tests (${failed.length})`);
  for (const f of failed) {
    out.push(`### ${f.title}\n- location: \`${f.file}\``);
    const r = f.results[f.results.length - 1] ?? {};
    for (const e of r.errors ?? []) {
      out.push('```\n' + strip(e.message ?? '').slice(0, 2500) + (e.location ? `\n    at ${e.location.file}:${e.location.line}` : '') + '\n```');
    }
    for (const a of r.attachments ?? []) if (a.path) out.push(`- attachment (${a.name}, ${a.contentType}): \`${path.relative(process.cwd(), a.path)}\``);
    if (f.results.length > 1) out.push(`- attempts: ${f.results.map((x) => x.status).join(', ')}`);
  }
  if (flaky.length) out.push(`## Flaky (passed on retry)\n` + flaky.map((f) => `- ${f.title} (\`${f.file}\`)`).join('\n'));
}

if (artifactsDir && fs.existsSync(path.join(artifactsDir, 'compose-logs.txt'))) {
  const lines = fs.readFileSync(path.join(artifactsDir, 'compose-logs.txt'), 'utf8').split('\n');
  const interesting = lines.filter((l) => /(ERROR|Exception|WARN .*(Security|JWT)|FATAL|panic|Caused by| 4\d\d | 5\d\d )/.test(l));
  out.push(`## Container log highlights (${interesting.length} lines, last 80; full file: \`${path.join(artifactsDir, 'compose-logs.txt')}\`)`);
  out.push('```\n' + interesting.slice(-80).map((l) => l.slice(0, 400)).join('\n') + '\n```');
}

process.stdout.write(out.join('\n\n') + '\n');
