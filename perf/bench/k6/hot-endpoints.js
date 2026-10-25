// Benchmark de latencia de los endpoints calientes (k6).
// Un endpoint por ejecución (ENDPOINT), VUs constantes durante DURATION.
//   ENDPOINT: feed | feedDeep | profile | comments | like   (rutas REST de cleanup-codebase)
// Toda la API exige sesión: setup() hace login (password común del dataset) del viewer
// y de un usuario por VU para el like, y cada petición lleva la cookie pc_session.
//   MODE:     serial (1 VU, peticiones secuenciales en caliente) | load (VUS concurrentes)
// Las ids salen de perf/bench/targets.mjs (el runner las pasa por env).
import http from 'k6/http';
import { check } from 'k6';
import { Trend, Rate } from 'k6/metrics';

const BASE = __ENV.BASE_URL || 'http://api:8080';
const ENDPOINT = __ENV.ENDPOINT || 'comments';
const MODE = __ENV.MODE || 'load';
const VUS = Number(__ENV.VUS || (MODE === 'serial' ? 1 : 10));
const DURATION = __ENV.DURATION || '30s';
const LABEL = __ENV.LABEL || 'baseline';
const T = {
  viewerId: __ENV.VIEWER_ID || '1',
  profileUserId: __ENV.PROFILE_USER_ID,
  commentsPostId: __ENV.COMMENTS_POST_ID,
  likePostId: Number(__ENV.LIKE_POST_ID),
  feedDeepCursor: __ENV.FEED_DEEP_CURSOR,
};
// Password del dataset sintético (perf/dataset.mjs), la pasa run-k6.sh.
const PASSWORD = __ENV.PERF_PASSWORD;
if (!PASSWORD) throw new Error('PERF_PASSWORD no definido (usar perf/bench/run-k6.sh)');

const dur = {
  main: new Trend('ep_duration', true),
  likeCreate: new Trend('like_create_duration', true),
  likeDelete: new Trend('like_delete_duration', true),
};
const errors = new Rate('ep_errors');

export const options = {
  scenarios: {
    hot: {
      executor: 'constant-vus',
      vus: VUS,
      duration: DURATION,
      // El feed actual tarda minutos por petición: se deja terminar la petición en curso.
      gracefulStop: __ENV.GRACEFUL_STOP || '20m',
    },
  },
  summaryTrendStats: ['min', 'med', 'avg', 'p(90)', 'p(95)', 'p(99)', 'max', 'count'],
  discardResponseBodies: true,
};

function login(userId) {
  const r = http.post(
    `${BASE}/api/auth/login`,
    JSON.stringify({ email: `perf.user${userId}@proconnect.test`, password: PASSWORD }),
    { headers: { 'Content-Type': 'application/json' }, responseType: 'none' },
  );
  const c = r.cookies.pc_session;
  if (r.status !== 200 || !c || !c.length) throw new Error(`login perf.user${userId} → HTTP ${r.status}`);
  return `pc_session=${c[0].value}`;
}

// Cookie del viewer (lecturas) y una por VU (like: usuario = nº de VU, 1..VUS).
export function setup() {
  const users = {};
  users[T.viewerId] = login(T.viewerId);
  if (ENDPOINT === 'like') for (let u = 1; u <= VUS; u++) users[u] = users[u] || login(u);
  return { users };
}

const params = (name, cookie) => ({ tags: { name }, timeout: __ENV.REQ_TIMEOUT || '20m', headers: { Cookie: cookie } });

function get(url, name, cookie) {
  const r = http.get(`${BASE}${url}`, params(name, cookie));
  const ok = check(r, { 'status 2xx': (x) => x.status >= 200 && x.status < 300 });
  errors.add(!ok);
  dur.main.add(r.timings.duration);
}

export default function (data) {
  const viewer = data.users[T.viewerId];
  switch (ENDPOINT) {
    case 'feed':
      return get('/api/posts?limit=20', 'feed', viewer);
    case 'feedDeep':
      return get(`/api/posts?limit=20&cursor=${T.feedDeepCursor}`, 'feedDeep', viewer);
    case 'profile':
      return get(`/api/users/${T.profileUserId}/profile`, 'profile', viewer);
    case 'comments':
      return get(`/api/posts/${T.commentsPostId}/comments`, 'comments', viewer);
    case 'like': {
      // Cada VU usa su propio usuario (1..64) sobre un post sin likes de esos usuarios:
      // create + delete deja la BD como estaba. Sin body: post de la ruta, usuario de la sesión.
      const cookie = data.users[__VU];
      const c = http.post(`${BASE}/api/posts/${T.likePostId}/likes`, null, params('likeCreate', cookie));
      const okC = check(c, { 'create 201': (x) => x.status === 201 });
      dur.likeCreate.add(c.timings.duration);
      dur.main.add(c.timings.duration);
      const d = http.del(`${BASE}/api/posts/${T.likePostId}/likes`, null, params('likeDelete', cookie));
      const okD = check(d, { 'delete 204': (x) => x.status === 204 });
      dur.likeDelete.add(d.timings.duration);
      errors.add(!(okC && okD));
      return;
    }
    default:
      throw new Error(`ENDPOINT desconocido: ${ENDPOINT}`);
  }
}

function pick(m) {
  if (!m) return null;
  const v = m.values;
  return { count: v.count, min: v.min, p50: v.med, avg: v.avg, p90: v['p(90)'], p95: v['p(95)'], p99: v['p(99)'], max: v.max };
}

export function handleSummary(data) {
  const out = {
    label: LABEL,
    endpoint: ENDPOINT,
    mode: MODE,
    vus: VUS,
    duration: DURATION,
    iterations: data.metrics.iterations ? data.metrics.iterations.values.count : 0,
    rps: data.metrics.http_reqs ? data.metrics.http_reqs.values.rate : 0,
    errorRate: data.metrics.ep_errors ? data.metrics.ep_errors.values.rate : null,
    latencyMs: ENDPOINT === 'like'
      ? { likeCreate: pick(data.metrics.like_create_duration), likeDelete: pick(data.metrics.like_delete_duration) }
      : { [ENDPOINT]: pick(data.metrics.ep_duration) },
  };
  const file = `/results/${LABEL}-k6-${ENDPOINT}-${MODE}.json`;
  return { [file]: JSON.stringify(out, null, 2), stdout: `${JSON.stringify(out)}\n` };
}
