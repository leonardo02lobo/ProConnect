## Resumen `after-v2`

| Endpoint | Sentencias SQL / req (DML · total) | Tiempo BD / req | Filas examinadas / req | Req. única en caliente (p50) | Serial 1 VU 30 s p50 / p95 (n) | Carga N VUs 30 s p50 / p95 (rps) | Tamaño respuesta | HTTP |
|---|---|---|---|---|---|---|---|---|
| `GET /api/posts (feed)` | **10001** · 10006 | 1586 ms | 275.258 | 5615 ms | 4452 ms / 5503 ms (7) | 91.6 s / 91.8 s (10 VUs, 0.1 rps) | 14.7 MB | 200 |
| `GET /api/users/{id}/profile` | **13** · 18 | 7.0 ms | 241 | 43.8 ms | 9.4 ms / 21.3 ms (2517) | 83.6 ms / 105 ms (10 VUs, 137.8 rps) | 13.6 kB | 200 |
| `GET /api/posts/{postId}/comments` | **2** · 7 | 1.0 ms | 65 | 13.1 ms | 2.4 ms / 3.4 ms (10756) | 6.9 ms / 47.8 ms (10 VUs, 868.7 rps) | 3.9 kB | 200 |
| `POST /api/posts/{postId}/likes` | **3** · 6 | 2.0 ms | 1 | 13.9 ms | 3.2 ms / 7.8 ms (3729) | 6.0 ms / 21.4 ms (10 VUs, 1121.4 rps) | 76 B | 201 |
| `DELETE /api/posts/{postId}/likes` | **3** · 6 | 2.0 ms | 3 | 13.1 ms | 2.9 ms / 6.6 ms (3729) | 5.8 ms / 21.2 ms (10 VUs, 1121.4 rps) | 0 B | 204 |

## Comparación `pre` → `after-v2`

| Endpoint | Sentencias DML (antes → después) | Serial p50 (antes → después) | Serial p95 (antes → después) | Carga p95 (antes → después) | Tamaño (antes → después) |
|---|---|---|---|---|---|
| `GET /api/posts (feed)` | 10001 → 10001 | 219.6 s → 4452 ms | 219.6 s → 5503 ms | 363.0 s → 91.8 s | 14.7 MB → 14.7 MB |
| `GET /api/users/{id}/profile` | 13 → 13 | 227 ms → 9.4 ms | 239 ms → 21.3 ms | 2599 ms → 105 ms | 13.6 kB → 13.6 kB |
| `GET /api/posts/{postId}/comments` | 2 → 2 | 13.5 ms → 2.4 ms | 15.7 ms → 3.4 ms | 111 ms → 47.8 ms | 3.9 kB → 3.9 kB |
| `POST /api/posts/{postId}/likes` | 3 → 3 | 11.4 ms → 3.2 ms | 15.3 ms → 7.8 ms | 95.5 ms → 21.4 ms | 76 B → 76 B |
| `DELETE /api/posts/{postId}/likes` | 3 → 3 | 11.5 ms → 2.9 ms | 15.5 ms → 6.6 ms | 95.2 ms → 21.2 ms | 0 B → 0 B |
