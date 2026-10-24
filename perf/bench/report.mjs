#!/usr/bin/env node
// Genera la tabla resumen (Markdown) a partir de perf/results/<label>-*.json.
// Uso: node bench/report.mjs <label> [--compare <labelBase>]
//   -> perf/results/<label>-summary.md (y stdout)
import fs from 'node:fs';
import path from 'node:path';

const dir = path.join(path.dirname(new URL(import.meta.url).pathname), '..', 'results');
const label = process.argv[2] ?? 'baseline';
const ci = process.argv.indexOf('--compare');
const base = ci > 0 ? process.argv[ci + 1] : null;

const ORDER = [
  ['feedViewer', 'GET /api/posts/{userId}', 'feedViewer'],
  ['feedAll', 'GET /api/posts', 'feedAll'],
  ['profile', 'GET /api/user/{userId}/profiles-posts', 'profile'],
  ['comments', 'GET /api/comment/find/{postId}', 'comments'],
  ['likeCreate', 'POST /api/like/create', 'like'],
  ['likeDelete', 'DELETE /api/like/delete', 'like'],
];

function load(lbl) {
  const files = fs.readdirSync(dir).filter((f) => f.startsWith(`${lbl}-`) && f.endsWith('.json') && (f.includes('-queries') || f.includes('-k6-')));
  const q = {};
  const k6 = {};
  for (const f of files) {
    const j = JSON.parse(fs.readFileSync(path.join(dir, f), 'utf8'));
    if (f.includes('-queries')) for (const r of j.results) q[r.key] = r;
    else if (f.includes('-k6-')) k6[`${j.endpoint}:${j.mode}`] = j;
  }
  return { q, k6 };
}

const ms = (v) => (v == null ? '—' : v >= 10_000 ? `${(v / 1000).toFixed(1)} s` : `${v.toFixed(v < 100 ? 1 : 0)} ms`);
const size = (b) => (b == null ? '—' : b >= 1e6 ? `${(b / 1e6).toFixed(1)} MB` : b >= 1e3 ? `${(b / 1e3).toFixed(1)} kB` : `${b} B`);

function row(data, key, k6ep) {
  const q = data.q[key];
  const lat = (mode) => {
    const k = data.k6[`${k6ep}:${mode}`];
    if (!k) return null;
    const m = k.latencyMs[key] ?? k.latencyMs[k6ep];
    return m ? { ...m, vus: k.vus, rps: k.rps, err: k.errorRate } : null;
  };
  return { q, serial: lat('serial'), load: lat('load') };
}

function table(lbl) {
  const data = load(lbl);
  const lines = [
    '| Endpoint | Sentencias SQL / req (DML · total) | Tiempo BD / req | Filas examinadas / req | Req. única en caliente (p50) | Serial 1 VU 30 s p50 / p95 (n) | Carga N VUs 30 s p50 / p95 (rps) | Tamaño respuesta | HTTP |',
    '|---|---|---|---|---|---|---|---|---|',
  ];
  for (const [key, name, k6ep] of ORDER) {
    const { q, serial, load: ld } = row(data, key, k6ep);
    if (!q && !serial && !ld) continue;
    lines.push(
      `| \`${name}\` | ${q ? `**${q.statementsDml}** · ${q.statementsTotal}` : '—'} | ${q ? ms(q.dbTimeMs) : '—'} | ${q ? q.rowsExamined.toLocaleString('es') : '—'} | ${q ? ms(q.warmLatencyMs.p50) : '—'} | ${serial ? `${ms(serial.p50)} / ${ms(serial.p95)} (${serial.count})` : '—'} | ${ld ? `${ms(ld.p50)} / ${ms(ld.p95)} (${ld.vus} VUs, ${ld.rps.toFixed(1)} rps${ld.err ? `, err ${(ld.err * 100).toFixed(1)} %` : ''})` : '—'} | ${q ? size(q.responseBytes) : '—'} | ${q ? [...new Set(q.status)].join('/') : '—'} |`,
    );
  }
  return lines.join('\n');
}

let md = `## Resumen \`${label}\`\n\n${table(label)}\n`;
if (base) {
  const a = load(base);
  const b = load(label);
  md += `\n## Comparación \`${base}\` → \`${label}\`\n\n| Endpoint | Sentencias (antes → después) | Serial p95 (antes → después) |\n|---|---|---|\n`;
  for (const [key, name, k6ep] of ORDER) {
    const ra = row(a, key, k6ep);
    const rb = row(b, key, k6ep);
    md += `| \`${name}\` | ${ra.q?.statementsDml ?? '—'} → ${rb.q?.statementsDml ?? '—'} | ${ms(ra.serial?.p95)} → ${ms(rb.serial?.p95)} |\n`;
  }
}
fs.writeFileSync(path.join(dir, `${label}-summary.md`), md);
console.log(md);
