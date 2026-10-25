## Resumen `pre`

| Endpoint | Sentencias SQL / req (DML · total) | Tiempo BD / req | Filas examinadas / req | Req. única en caliente (p50) | Serial 1 VU 30 s p50 / p95 (n) | Carga N VUs 30 s p50 / p95 (rps) | Tamaño respuesta | HTTP |
|---|---|---|---|---|---|---|---|---|
| `GET /api/posts (feed)` | **10001** · 10006 | 211.9 s | 1.704.974.868 | 218.0 s | 219.6 s / 219.6 s (1) | 362.9 s / 363.0 s (3 VUs, 0.0 rps) | 14.7 MB | 200 |
| `GET /api/users/{id}/profile` | **13** · 18 | 209 ms | 1.684.768 | 246 ms | 227 ms / 239 ms (132) | 1893 ms / 2599 ms (10 VUs, 5.2 rps) | 13.6 kB | 200 |
| `GET /api/posts/{postId}/comments` | **2** · 7 | 10.0 ms | 59.539 | 20.2 ms | 13.5 ms / 15.7 ms (2130) | 93.6 ms / 111 ms (10 VUs, 114.5 rps) | 3.9 kB | 200 |
| `POST /api/posts/{postId}/likes` | **3** · 6 | 8.0 ms | 50.001 | 19.5 ms | 11.4 ms / 15.3 ms (1214) | 79.5 ms / 95.5 ms (10 VUs, 151.0 rps) | 76 B | 201 |
| `DELETE /api/posts/{postId}/likes` | **3** · 6 | 9.0 ms | 50.003 | 19.8 ms | 11.5 ms / 15.5 ms (1214) | 80.6 ms / 95.2 ms (10 VUs, 151.0 rps) | 0 B | 204 |

## Comparación `baseline` → `pre`

| Endpoint | Sentencias DML (antes → después) | Serial p50 (antes → después) | Serial p95 (antes → después) | Carga p95 (antes → después) | Tamaño (antes → después) |
|---|---|---|---|---|---|
| `GET /api/posts (feed)` | 40001 → 10001 | 240.9 s → 219.6 s | 240.9 s → 219.6 s | 338.7 s → 363.0 s | 18.5 MB → 14.7 MB |
| `GET /api/users/{id}/profile` | 53 → 13 | 445 ms → 227 ms | 517 ms → 239 ms | 4089 ms → 2599 ms | 16.0 kB → 13.6 kB |
| `GET /api/posts/{postId}/comments` | 1 → 2 | 7.3 ms → 13.5 ms | 8.6 ms → 15.7 ms | 75.9 ms → 111 ms | 2.1 kB → 3.9 kB |
| `POST /api/posts/{postId}/likes` | 2 → 3 | 11.0 ms → 11.4 ms | 15.4 ms → 15.3 ms | 81.0 ms → 95.5 ms | 23 B → 76 B |
| `DELETE /api/posts/{postId}/likes` | 3 → 3 | 11.7 ms → 11.5 ms | 16.3 ms → 15.5 ms | 82.1 ms → 95.2 ms | 25 B → 0 B |
