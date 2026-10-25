#!/usr/bin/env node
// Cuenta las sentencias SQL que ejecuta la API por petición, SIN instrumentar la
// app: antes de cada petición se vacían las tablas de resumen de
// performance_schema y después se suman las sentencias del usuario MySQL `app`
// (el único que usa la API). La conexión de medición usa `root`, así que sus
// propias consultas no contaminan el conteo.
//
// Además mide latencia de la petición (cliente HTTP local, en caliente tras un
// warm-up), tamaño de respuesta, tiempo total en BD y las sentencias más
// repetidas (por digest) para diagnosticar N+1.
//
// Uso: node bench/count-queries.mjs [--label baseline] [--only feedViewer,profile]
//                                   [--repeat 3] [--warmup 1]
// Env: API_URL (http://127.0.0.1:28080), DB_* (ver targets.mjs)
import fs from 'node:fs';
import path from 'node:path';
import mysql from 'mysql2/promise';
import { dbConfig, login, resolveTargets } from './targets.mjs';

const args = process.argv.slice(2);
const arg = (name, def) => {
  const i = args.indexOf(`--${name}`);
  return i >= 0 ? args[i + 1] : def;
};
const API = process.env.API_URL ?? 'http://127.0.0.1:28080';
const APP_DB_USER = process.env.APP_DB_USER ?? 'app';
const LABEL = arg('label', 'baseline');
const ONLY = arg('only', '')
  .split(',')
  .filter(Boolean);
const REPEAT = Number(arg('repeat', 3));
const WARMUP = Number(arg('warmup', 1));
const TIMEOUT_MS = Number(process.env.REQUEST_TIMEOUT_MS ?? 900_000);

// Rutas REST de cleanup-codebase (docs/api-routes.md). El viewer y el autor del like
// salen de la sesión (cookie pc_session del usuario `likeUserId` = viewer).
// Equivalencias con la línea base (fase 1): feed ↔ feedViewer, profile ↔ profile, etc.
function endpoints(t) {
  return [
    { key: 'feed', name: 'GET /api/posts (feed, 1.ª página)', method: 'GET', url: '/api/posts?limit=20' },
    { key: 'feedDeep', name: `GET /api/posts?cursor (feed, página tras ${t.feedDeepOffset} posts)`, method: 'GET', url: `/api/posts?limit=20&cursor=${t.feedDeepCursor}` },
    { key: 'profile', name: 'GET /api/users/{id}/profile (perfil)', method: 'GET', url: `/api/users/${t.profileUserId}/profile` },
    { key: 'comments', name: 'GET /api/posts/{postId}/comments (comentarios)', method: 'GET', url: `/api/posts/${t.commentsPostId}/comments` },
    // like: cada repetición es create + delete del mismo par, para dejar la BD limpia.
    { key: 'likeCreate', name: 'POST /api/posts/{postId}/likes', method: 'POST', url: `/api/posts/${t.likePostId}/likes`, pairedWith: 'likeDelete' },
    { key: 'likeDelete', name: 'DELETE /api/posts/{postId}/likes', method: 'DELETE', url: `/api/posts/${t.likePostId}/likes`, paired: true },
  ];
}

let COOKIE = '';

async function http(ep) {
  const init = { method: ep.method, signal: AbortSignal.timeout(TIMEOUT_MS), headers: { Cookie: COOKIE } };
  const t0 = process.hrtime.bigint();
  const res = await fetch(API + ep.url, init);
  const buf = Buffer.from(await res.arrayBuffer());
  const ms = Number(process.hrtime.bigint() - t0) / 1e6;
  return { status: res.status, bytes: buf.length, ms, sample: buf.subarray(0, 160).toString() };
}

async function resetCounters(conn) {
  await conn.query('TRUNCATE TABLE performance_schema.events_statements_summary_by_user_by_event_name');
  await conn.query('TRUNCATE TABLE performance_schema.events_statements_summary_by_digest');
}

async function readCounters(conn) {
  const [byEvent] = await conn.query(
    `SELECT EVENT_NAME ev, COUNT_STAR n, SUM_TIMER_WAIT ps, SUM_ROWS_EXAMINED rx, SUM_ROWS_SENT rs
       FROM performance_schema.events_statements_summary_by_user_by_event_name
      WHERE USER = ? AND COUNT_STAR > 0 AND EVENT_NAME LIKE 'statement/sql/%'`,
    [APP_DB_USER],
  );
  const [digests] = await conn.query(
    `SELECT LEFT(DIGEST_TEXT, 160) q, COUNT_STAR n, ROUND(SUM_TIMER_WAIT/1e9, 1) ms,
            SUM_ROWS_EXAMINED rx, SUM_ROWS_SENT rs
       FROM performance_schema.events_statements_summary_by_digest
      WHERE SCHEMA_NAME = ? AND COUNT_STAR > 0
        AND DIGEST_TEXT NOT LIKE '%performance_schema%'
      ORDER BY COUNT_STAR DESC, SUM_TIMER_WAIT DESC LIMIT 8`,
    [dbConfig.database],
  );
  const events = Object.fromEntries(byEvent.map((r) => [r.ev.replace('statement/sql/', ''), Number(r.n)]));
  const total = byEvent.reduce((a, r) => a + Number(r.n), 0);
  const dml = ['select', 'insert', 'update', 'delete'].reduce((a, k) => a + (events[k] ?? 0), 0);
  const dbMs = byEvent.reduce((a, r) => a + Number(r.ps), 0) / 1e9;
  const rowsExamined = byEvent.reduce((a, r) => a + Number(r.rx), 0);
  return {
    statementsTotal: total,
    statementsDml: dml,
    events,
    dbTimeMs: Math.round(dbMs),
    rowsExamined,
    topDigests: digests.map((d) => ({ query: d.q, count: Number(d.n), ms: Number(d.ms), rowsExamined: Number(d.rx), rowsSent: Number(d.rs) })),
  };
}

const pct = (arr, p) => {
  const s = [...arr].sort((a, b) => a - b);
  return s[Math.min(s.length - 1, Math.ceil((p / 100) * s.length) - 1)];
};

async function measure(conn, ep, eps) {
  const pair = ep.pairedWith ? eps.find((e) => e.key === ep.pairedWith) : null;
  // Warm-up (no se mide)
  for (let i = 0; i < WARMUP; i++) {
    await http(ep);
    if (pair) await http(pair);
  }
  const runs = [];
  const pairRuns = [];
  for (let i = 0; i < REPEAT; i++) {
    await resetCounters(conn);
    const r = await http(ep);
    const c = await readCounters(conn);
    runs.push({ ...r, ...c });
    process.stderr.write(`  ${ep.key} #${i + 1}: ${r.status} ${r.ms.toFixed(1)} ms, ${c.statementsDml} DML / ${c.statementsTotal} total, ${r.bytes} B\n`);
    if (pair) {
      await resetCounters(conn);
      const r2 = await http(pair);
      const c2 = await readCounters(conn);
      pairRuns.push({ ...r2, ...c2 });
      process.stderr.write(`  ${pair.key} #${i + 1}: ${r2.status} ${r2.ms.toFixed(1)} ms, ${c2.statementsDml} DML / ${c2.statementsTotal} total\n`);
    }
  }
  const summarize = (e, rs) => ({
    key: e.key,
    endpoint: e.name,
    url: e.url,
    status: rs.map((r) => r.status),
    statementsDml: rs[rs.length - 1].statementsDml,
    statementsTotal: rs[rs.length - 1].statementsTotal,
    statementsStable: new Set(rs.map((r) => r.statementsDml)).size === 1,
    warmLatencyMs: { min: Math.min(...rs.map((r) => r.ms)), p50: pct(rs.map((r) => r.ms), 50), max: Math.max(...rs.map((r) => r.ms)), samples: rs.map((r) => Math.round(r.ms * 10) / 10) },
    dbTimeMs: rs[rs.length - 1].dbTimeMs,
    rowsExamined: rs[rs.length - 1].rowsExamined,
    responseBytes: rs[rs.length - 1].bytes,
    events: rs[rs.length - 1].events,
    topDigests: rs[rs.length - 1].topDigests,
    responseSample: rs[rs.length - 1].sample,
  });
  const out = [summarize(ep, runs)];
  if (pair) out.push(summarize(pair, pairRuns));
  return out;
}

async function main() {
  const conn = await mysql.createConnection(dbConfig);
  const targets = await resolveTargets(conn);
  COOKIE = await login(API, targets.viewerEmail);
  const eps = endpoints(targets);
  const selected = eps.filter((e) => !e.paired && (ONLY.length === 0 || ONLY.includes(e.key)));
  const results = [];
  for (const ep of selected) {
    process.stderr.write(`→ ${ep.name}\n`);
    results.push(...(await measure(conn, ep, eps)));
  }
  await conn.end();
  const outDir = path.join(path.dirname(new URL(import.meta.url).pathname), '..', 'results');
  fs.mkdirSync(outDir, { recursive: true });
  const suffix = ONLY.length ? `-${ONLY.join('+')}` : '';
  const file = path.join(outDir, `${LABEL}-queries${suffix}.json`);
  const payload = { label: LABEL, api: API, measuredAt: new Date().toISOString(), repeat: REPEAT, warmup: WARMUP, targets, results };
  fs.writeFileSync(file, JSON.stringify(payload, null, 2));
  console.log(`Guardado ${file}`);
  console.table(results.map((r) => ({ endpoint: r.key, dml: r.statementsDml, total: r.statementsTotal, p50ms: Math.round(r.warmLatencyMs.p50), dbMs: r.dbTimeMs, bytes: r.responseBytes, status: r.status.join('/') })));
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
