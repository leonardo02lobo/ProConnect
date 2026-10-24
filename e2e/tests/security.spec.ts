import { request } from '@playwright/test';
import { expect, NO_SESSION, test } from '../fixtures/test';
import { API_URL, SESSION_COOKIE, WEB_URL } from '../fixtures/env';
import { TEST_IMAGE } from '../fixtures/api';
import fs from 'node:fs';

// verify-web-security (tarea 6.6): la sesión no es legible desde JS y la CSP en enforce no rompe la app.

test('document.cookie does not expose the session cookie', async ({ page, context }) => {
  await page.goto('/');
  const cookies = await context.cookies();
  const session = cookies.find((c) => c.name === SESSION_COOKIE);
  expect(session, 'the browser holds the session cookie').toBeDefined();
  expect(session?.httpOnly).toBe(true);
  expect(session?.sameSite).toBe('Lax');
  expect(await page.evaluate(() => document.cookie)).not.toContain(SESSION_COOKIE);
});

test('the enforced CSP does not break the feed (no violations while browsing and posting)', async ({ page, feed, uid }) => {
  const violations: string[] = [];
  page.on('console', (msg) => {
    if (/Content[- ]Security[- ]Policy/i.test(msg.text())) violations.push(msg.text());
  });
  await page.addInitScript(() => {
    document.addEventListener('securitypolicyviolation', (e) => {
      console.error(`Content-Security-Policy violation: ${e.violatedDirective} ${e.blockedURI}`);
    });
  });

  const res = await page.goto('/');
  const csp = res?.headers()['content-security-policy'] ?? '';
  expect(csp).toContain("frame-ancestors 'none'");
  expect(csp).toMatch(/script-src 'self'/);
  expect(csp).not.toContain('unsafe-eval');
  expect(res?.headers()['x-frame-options']).toBe('DENY');

  await feed.goto();
  const title = `CSP check ${uid}`;
  await page.getByTestId('add-post-title').fill(title);
  await page.getByTestId('add-post-content').fill('created under an enforced CSP');
  await page.getByTestId('add-post-image').setInputFiles(TEST_IMAGE);
  await page.getByTestId('add-post-submit').click();
  const card = feed.post(title);
  await expect(card).toHaveCount(1);
  await expect
    .poll(() => card.getByTestId('post-image').evaluate((img: HTMLImageElement) => img.complete && img.naturalWidth > 0))
    .toBe(true);
  await page.goto('/Profile');
  expect(violations, violations.join('\n')).toEqual([]);
});

test('gateway sends security headers and rejects cross-site mutations', async ({ aliceApi }) => {
  const health = await aliceApi.ctx.get('/actuator/health');
  expect(health.headers()['content-security-policy']).toContain("frame-ancestors 'none'");
  expect(health.headers()['x-content-type-options']).toBe('nosniff');

  const evil = await aliceApi.ctx.post('/api/posts/create', {
    headers: { Origin: 'https://evil.example' },
    data: { title: 'csrf', content: 'csrf' },
  });
  expect(evil.status()).toBe(403);
  const fromApp = await aliceApi.ctx.post('/api/posts/create', {
    headers: { Origin: WEB_URL },
    data: { title: 'same-site ok', content: 'x' },
  });
  expect(fromApp.status()).toBe(201);
});

test.describe('without session', () => {
  test.use({ storageState: NO_SESSION });

  test('media upload and read are rejected with 401', async () => {
    const anon = await request.newContext({ baseURL: API_URL });
    const upload = await anon.post('/upload/subirImagen', {
      multipart: { miArchivo: { name: 'x.png', mimeType: 'image/png', buffer: fs.readFileSync(TEST_IMAGE) } },
    });
    expect(upload.status()).toBe(401);
    expect((await anon.get('/upload/uploads/anything.png')).status()).toBe(401);
    await anon.dispose();
  });
});

test('media rejects disguised HTML with 415', async ({ aliceApi }) => {
  const res = await aliceApi.ctx.post('/upload/subirImagen', {
    multipart: { miArchivo: { name: 'evil.png', mimeType: 'image/png', buffer: Buffer.from('<script>alert(1)</script>') } },
  });
  expect(res.status()).toBe(415);
});
