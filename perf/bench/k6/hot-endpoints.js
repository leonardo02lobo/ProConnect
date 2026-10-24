// Benchmark de latencia de los endpoints calientes (k6).
// Un endpoint por ejecución (ENDPOINT), VUs constantes durante DURATION.
//   ENDPOINT: feedViewer | feedAll | profile | comments | like
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
};

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

const params = (name) => ({ tags: { name }, timeout: __ENV.REQ_TIMEOUT || '20m' });
const jsonParams = (name) => ({ ...params(name), headers: { 'Content-Type': 'application/json' } });

function get(url, name) {
  const r = http.get(`${BASE}${url}`, params(name));
  const ok = check(r, { 'status 2xx': (x) => x.status >= 200 && x.status < 300 });
  errors.add(!ok);
  dur.main.add(r.timings.duration);
}

export default function () {
  switch (ENDPOINT) {
    case 'feedViewer':
      return get(`/api/posts/${T.viewerId}`, 'feedViewer');
    case 'feedAll':
      return get('/api/posts', 'feedAll');
    case 'profile':
      return get(`/api/user/${T.profileUserId}/profiles-posts`, 'profile');
    case 'comments':
      return get(`/api/comment/find/${T.commentsPostId}`, 'comments');
    case 'like': {
      // Cada VU usa su propio usuario (1..64) sobre un post sin likes de esos usuarios:
      // create + delete deja la BD como estaba.
      const body = JSON.stringify({ userId: __VU, postId: T.likePostId });
      const c = http.post(`${BASE}/api/like/create`, body, jsonParams('likeCreate'));
      const okC = check(c, { 'create 201': (x) => x.status === 201 });
      dur.likeCreate.add(c.timings.duration);
      dur.main.add(c.timings.duration);
      const d = http.del(`${BASE}/api/like/delete`, body, jsonParams('likeDelete'));
      const okD = check(d, { 'delete 200': (x) => x.status === 200 });
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
