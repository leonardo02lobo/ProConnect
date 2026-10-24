## Resumen `baseline`

| Endpoint | Sentencias SQL / req (DML · total) | Tiempo BD / req | Filas examinadas / req | Req. única en caliente (p50) | Serial 1 VU 30 s p50 / p95 (n) | Carga N VUs 30 s p50 / p95 (rps) | Tamaño respuesta | HTTP |
|---|---|---|---|---|---|---|---|---|
| `GET /api/posts/{userId}` | **40001** · 40001 | 213.1 s | 1.627.680.922 | 223.7 s | 240.9 s / 240.9 s (1) | 338.3 s / 338.7 s (3 VUs, 0.0 rps) | 18.5 MB | 200 |
| `GET /api/posts` | **40001** · 40001 | 146.4 s | 1.127.680.922 | 155.7 s | 171.8 s / 171.8 s (1) | 257.4 s / 257.4 s (3 VUs, 0.0 rps) | 18.5 MB | 200 |
| `GET /api/user/{userId}/profiles-posts` | **53** · 183 | 388 ms | 3.444.946 | 448 ms | 445 ms / 517 ms (67) | 2718 ms / 4089 ms (10 VUs, 3.2 rps) | 16.0 kB | 200 |
| `GET /api/comment/find/{postId}` | **1** · 1 | 6.0 ms | 30.000 | 9.9 ms | 7.3 ms / 8.6 ms (3876) | 30.1 ms / 75.9 ms (10 VUs, 244.1 rps) | 2.1 kB | 200 |
| `POST /api/like/create` | **2** · 5 | 8.0 ms | 50.000 | 14.9 ms | 11.0 ms / 15.4 ms (1221) | 48.0 ms / 81.0 ms (10 VUs, 199.2 rps) | 23 B | 201 |
| `DELETE /api/like/delete` | **3** · 6 | 9.0 ms | 50.003 | 15.5 ms | 11.7 ms / 16.3 ms (1221) | 50.6 ms / 82.1 ms (10 VUs, 199.2 rps) | 25 B | 200 |
