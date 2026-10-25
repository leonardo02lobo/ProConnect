## Resumen `after`

| Endpoint | Sentencias SQL / req (DML · total) | Tiempo BD / req | Filas examinadas / req | Req. única en caliente (p50) | Serial 1 VU 30 s p50 / p95 (n) | Carga N VUs 30 s p50 / p95 (rps) | Tamaño respuesta | HTTP |
|---|---|---|---|---|---|---|---|---|
| `GET /api/posts (feed)` | **3** · 8 | 1.0 ms | 173 | 6.3 ms | 3.8 ms / 4.9 ms (7141) | 8.1 ms / 64.1 ms (10 VUs, 551.3 rps) | 26.8 kB | 200 |
| `GET /api/posts?cursor= (página profunda)` | **3** · 8 | 1.0 ms | 139 | 5.6 ms | 3.7 ms / 4.5 ms (7510) | 7.7 ms / 62.9 ms (10 VUs, 587.5 rps) | 23.6 kB | 200 |
| `GET /api/users/{id}/profile` | **4** · 9 | 3.0 ms | 61 | 20.7 ms | 4.6 ms / 9.6 ms (5350) | 9.6 ms / 73.9 ms (10 VUs, 419.1 rps) | 12.8 kB | 200 |
| `GET /api/posts/{postId}/comments` | **1** · 6 | 1.0 ms | 24 | 9.7 ms | 1.5 ms / 2.0 ms (16786) | 2.7 ms / 52.6 ms (10 VUs, 1564.0 rps) | 3.8 kB | 200 |
| `POST /api/posts/{postId}/likes` | **2** · 5 | 2.0 ms | 1 | 11.3 ms | 3.0 ms / 6.1 ms (4002) | 31.7 ms / 48.2 ms (10 VUs, 273.9 rps) | 77 B | 201 |
| `DELETE /api/posts/{postId}/likes` | **2** · 5 | 2.0 ms | 2 | 10.3 ms | 2.5 ms / 4.8 ms (4002) | 31.0 ms / 50.4 ms (10 VUs, 273.9 rps) | 0 B | 204 |

## Comparación `baseline` → `after`

| Endpoint | Sentencias DML (antes → después) | Serial p50 (antes → después) | Serial p95 (antes → después) | Carga p95 (antes → después) | Tamaño (antes → después) |
|---|---|---|---|---|---|
| `GET /api/posts (feed)` | 40001 → 3 | 240.9 s → 3.8 ms | 240.9 s → 4.9 ms | 338.7 s → 64.1 ms | 18.5 MB → 26.8 kB |
| `GET /api/posts?cursor= (página profunda)` | 40001 → 3 | 240.9 s → 3.7 ms | 240.9 s → 4.5 ms | 338.7 s → 62.9 ms | 18.5 MB → 23.6 kB |
| `GET /api/users/{id}/profile` | 53 → 4 | 445 ms → 4.6 ms | 517 ms → 9.6 ms | 4089 ms → 73.9 ms | 16.0 kB → 12.8 kB |
| `GET /api/posts/{postId}/comments` | 1 → 1 | 7.3 ms → 1.5 ms | 8.6 ms → 2.0 ms | 75.9 ms → 52.6 ms | 2.1 kB → 3.8 kB |
| `POST /api/posts/{postId}/likes` | 2 → 2 | 11.0 ms → 3.0 ms | 15.4 ms → 6.1 ms | 81.0 ms → 48.2 ms | 23 B → 77 B |
| `DELETE /api/posts/{postId}/likes` | 3 → 2 | 11.7 ms → 2.5 ms | 16.3 ms → 4.8 ms | 82.1 ms → 50.4 ms | 25 B → 0 B |
