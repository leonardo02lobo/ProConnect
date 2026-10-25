#!/usr/bin/env node
// Snapshot of the *shape* of the public JSON contracts (upgrade-spring-boot-4, tasks 1.2 / 4.3).
//
//   node e2e/scripts/contract-snapshot.mjs <out.json>          # take a snapshot
//   node e2e/scripts/contract-snapshot.mjs --diff <a> <b>      # compare two snapshots (exit 1 if different)
//
// Runs against a live stack through the gateway (E2E_API_URL, default http://localhost:18080) and
// needs the bootstrap ADMIN of docker-compose.e2e.yml (E2E_ADMIN_EMAIL / E2E_ADMIN_PASSWORD).
// Values are replaced by their type ("string", "number", "boolean", "null"), ISO-8601 strings by
// "datetime:<format>" (so a change in the date serialization is a diff), arrays by the shape of
// their elements and problem+json bodies keep the status, title and content type. Each request
// uses fresh users, so the snapshot is independent of the data already in the stack.
import { randomBytes } from 'node:crypto';
import fs from 'node:fs';

const API = (process.env.E2E_API_URL ?? 'http://localhost:18080').replace(/\/$/, '');
const ADMIN_EMAIL = process.env.E2E_ADMIN_EMAIL ?? 'admin.e2e@proconnect.test';
const ADMIN_PASSWORD = process.env.E2E_ADMIN_PASSWORD ?? 'E2e-Admin-Passw0rd!';
const PASSWORD = 'Contract-Passw0rd!';

function shapeOfString(value) {
  if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(\.\d+)?(Z|[+-]\d{2}:\d{2})$/.test(value)) {
    const frac = value.match(/\.(\d+)/);
    const zone = value.endsWith('Z') ? 'Z' : 'offset';
    return `datetime:iso${frac ? `.frac` : ''}${zone}`;
  }
  return 'string';
}

export function shape(value) {
  if (value === null) return 'null';
  if (Array.isArray(value)) {
    if (value.length === 0) return ['<empty>'];
    // Union of the element shapes (deduplicated), so optional/nullable fields are visible.
    const seen = new Map();
    for (const item of value) {
      const s = shape(item);
      seen.set(JSON.stringify(s), s);
    }
    return [...seen.entries()].sort(([a], [b]) => a.localeCompare(b)).map(([, s]) => s);
  }
  switch (typeof value) {
    case 'string':
      return shapeOfString(value);
    case 'number':
      return 'number';
    case 'boolean':
      return 'boolean';
    case 'object': {
      const out = {};
      for (const key of Object.keys(value).sort()) out[key] = shape(value[key]);
      return out;
    }
    default:
      return typeof value;
  }
}

class Client {
  constructor() {
    this.cookie = null;
  }
  async req(method, path, body) {
    const headers = { Accept: 'application/json' };
    if (body !== undefined) headers['Content-Type'] = 'application/json';
    if (this.cookie) headers.Cookie = this.cookie;
    const res = await fetch(API + path, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
      redirect: 'manual',
    });
    const setCookie = res.headers.getSetCookie?.() ?? [];
    for (const c of setCookie) {
      const m = c.match(/^pc_session=([^;]*)/);
      if (m) this.cookie = m[1] ? `pc_session=${m[1]}` : null;
    }
    const text = await res.text();
    let json = null;
    try {
      json = text.trim() ? JSON.parse(text) : null;
    } catch {
      json = { '<non-json>': text.slice(0, 80) };
    }
    return { status: res.status, type: (res.headers.get('content-type') ?? '').split(';')[0], json };
  }
}

async function main() {
  const suffix = randomBytes(6).toString('hex');
  const snap = {};
  // `only` keeps just the list items created by this run (lists also return other data in the stack).
  const record = (name, r, expected, only) => {
    if (only && r.json && Array.isArray(r.json.items)) r = { ...r, json: { ...r.json, items: r.json.items.filter(only) } };
    if (expected !== undefined && r.status !== expected) {
      throw new Error(`${name}: expected HTTP ${expected}, got ${r.status} ${JSON.stringify(r.json)}`);
    }
    const entry = { status: r.status, contentType: r.type, body: shape(r.json) };
    if (r.type === 'application/problem+json' && r.json) entry.title = r.json.title;
    snap[name] = entry;
    return r.json;
  };

  const ana = new Client();
  const bob = new Client();
  const user = (n) => ({ name: `${n} ${suffix}`, userName: `${n}_${suffix}`, email: `${n}.${suffix}@contract.test`, password: PASSWORD });
  record('POST /api/auth/register', await ana.req('POST', '/api/auth/register', user('ana')), 201);
  record('POST /api/auth/register (duplicate 409)', await ana.req('POST', '/api/auth/register', user('ana')), 409);
  record('POST /api/auth/register (invalid 400)', await ana.req('POST', '/api/auth/register', { name: '', userName: '', email: 'x', password: '1' }), 400);
  await bob.req('POST', '/api/auth/register', user('bob'));
  record('POST /api/auth/login (bad 401)', await new Client().req('POST', '/api/auth/login', { email: user('ana').email, password: 'wrong-password' }), 401);
  record('POST /api/auth/login', await ana.req('POST', '/api/auth/login', { email: user('ana').email, password: PASSWORD }), 200);
  await bob.req('POST', '/api/auth/login', { email: user('bob').email, password: PASSWORD });

  const me = record('GET /api/auth/me', await ana.req('GET', '/api/auth/me'), 200);
  const bobMe = (await bob.req('GET', '/api/auth/me')).json;
  record('GET /api/auth/me (anonymous 401)', await new Client().req('GET', '/api/auth/me'), 401);
  record('PUT /api/users/{id}/media', await ana.req('PUT', `/api/users/${me.id}/media`, { avatarUrl: 'avatar.png', coverUrl: 'cover.jpg' }), 200);
  record('PUT /api/users/{id}/media (other user 403)', await bob.req('PUT', `/api/users/${me.id}/media`, { avatarUrl: 'x.png' }), 403);

  const post = record('POST /api/posts', await ana.req('POST', '/api/posts', { title: `t ${suffix}`, content: 'contenido', imageUrl: 'pic.png' }), 201);
  record('POST /api/posts (invalid 400)', await ana.req('POST', '/api/posts', { title: '', content: '' }), 400);
  record('POST /api/posts/{id}/likes', await bob.req('POST', `/api/posts/${post.id}/likes`), 201);
  record('POST /api/posts/{id}/likes (duplicate 409)', await bob.req('POST', `/api/posts/${post.id}/likes`), 409);
  record('POST /api/posts/{id}/comments', await bob.req('POST', `/api/posts/${post.id}/comments`, { content: 'hola' }), 201);
  record('GET /api/posts (feed)', await bob.req('GET', '/api/posts?limit=5'), 200, (p) => p.id === post.id);
  record('GET /api/posts?cursor=bad (400)', await bob.req('GET', '/api/posts?cursor=not-a-cursor'), 400);
  record('GET /api/posts/{id}', await bob.req('GET', `/api/posts/${post.id}`), 200);
  record('GET /api/posts/{id} (404)', await bob.req('GET', '/api/posts/999999999'), 404);
  record('GET /api/posts/{id}/comments', await bob.req('GET', `/api/posts/${post.id}/comments`), 200);
  record('GET /api/posts/{id}/likes', await bob.req('GET', `/api/posts/${post.id}/likes`), 200);
  record('GET /api/users/{id}/profile', await bob.req('GET', `/api/users/${me.id}/profile`), 200);
  record('GET /api/users/{id}/profile (404)', await bob.req('GET', '/api/users/999999999/profile'), 404);
  record('GET /api/users/{id}/posts', await bob.req('GET', `/api/users/${me.id}/posts`), 200);
  record('GET /api/users?q=', await bob.req('GET', `/api/users?q=${suffix}`), 200);
  record('GET /api/users/{id}', await bob.req('GET', `/api/users/${me.id}`), 200);

  record('POST /api/friends/requests', await ana.req('POST', '/api/friends/requests', { userId: bobMe.id }), 201);
  record('GET /api/friends/requests', await bob.req('GET', '/api/friends/requests'), 200);
  record('POST /api/messages', await ana.req('POST', '/api/messages', { receiverId: bobMe.id, content: 'hola' }), 201);
  record('GET /api/messages', await bob.req('GET', '/api/messages'), 200);
  record('POST /api/companies (USER 403)', await ana.req('POST', '/api/companies', { name: 'x', description: 'y', location: 'z' }), 403);

  const admin = new Client();
  await admin.req('POST', '/api/auth/login', { email: ADMIN_EMAIL, password: ADMIN_PASSWORD });
  record('GET /api/admin/users', await admin.req('GET', `/api/admin/users?q=${suffix}`), 200);
  record('PUT /api/admin/users/{id}/role', await admin.req('PUT', `/api/admin/users/${me.id}/role`, { role: 'COMPANY_ADMIN' }), 200);
  record('PUT /api/admin/users/{id}/role (invalid 400)', await admin.req('PUT', `/api/admin/users/${me.id}/role`, { role: 'admin' }), 400);
  record('GET /api/admin/users (USER 403)', await bob.req('GET', '/api/admin/users'), 403);

  const company = record('POST /api/companies', await ana.req('POST', '/api/companies', { name: `c ${suffix}`, description: 'd', location: 'l' }), 201);
  const job = record('POST /api/jobs', await ana.req('POST', '/api/jobs', { companyId: company.id, title: 'dev', description: 'd', workStation: 'w' }), 201);
  record('GET /api/jobs/{id}', await bob.req('GET', `/api/jobs/${job.id}`), 200);
  const postulation = record('POST /api/postulations', await bob.req('POST', '/api/postulations', { jobId: job.id }), 201);
  record('POST /api/postulations (duplicate 409)', await bob.req('POST', '/api/postulations', { jobId: job.id }), 409);
  record('GET /api/postulations/{id}', await bob.req('GET', `/api/postulations/${postulation.id}`), 200);
  record('GET /api/jobs/{id}/postulations', await ana.req('GET', `/api/jobs/${job.id}/postulations`), 200);
  record('GET /api/companies', await bob.req('GET', '/api/companies?size=100'), 200, (c) => c.id === company.id);

  record('POST /api/auth/logout', await ana.req('POST', '/api/auth/logout'), 204);
  return snap;
}

function diff(a, b, path = '$', out = []) {
  if (JSON.stringify(a) === JSON.stringify(b)) return out;
  if (a && b && typeof a === 'object' && typeof b === 'object' && !Array.isArray(a) && !Array.isArray(b)) {
    for (const k of new Set([...Object.keys(a), ...Object.keys(b)])) {
      if (!(k in a)) out.push(`+ ${path}.${k}: ${JSON.stringify(b[k])}`);
      else if (!(k in b)) out.push(`- ${path}.${k}: ${JSON.stringify(a[k])}`);
      else diff(a[k], b[k], `${path}.${k}`, out);
    }
    return out;
  }
  out.push(`~ ${path}: ${JSON.stringify(a)} -> ${JSON.stringify(b)}`);
  return out;
}

const args = process.argv.slice(2);
if (args[0] === '--diff') {
  const [a, b] = args.slice(1).map((f) => JSON.parse(fs.readFileSync(f, 'utf8')));
  const changes = diff(a, b);
  console.log(changes.length ? changes.join('\n') : 'Same shape: no differences.');
  process.exit(changes.length ? 1 : 0);
} else if (args[0]) {
  const snap = await main();
  fs.writeFileSync(args[0], JSON.stringify(snap, null, 2) + '\n');
  console.log(`Wrote ${Object.keys(snap).length} contracts to ${args[0]}`);
} else {
  console.error('usage: contract-snapshot.mjs <out.json> | --diff <a.json> <b.json>');
  process.exit(2);
}
