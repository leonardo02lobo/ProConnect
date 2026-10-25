// Elige de forma determinista los ids sobre los que se mide cada endpoint.
// Como el dataset es determinista, los ids resultantes son siempre los mismos;
// se calculan por SQL para que el criterio quede documentado y sea reutilizable
// tras el refactor (fase 2).
import mysql from 'mysql2/promise';

export const dbConfig = {
  host: process.env.DB_HOST ?? '127.0.0.1',
  port: Number(process.env.DB_PORT ?? 23306),
  user: process.env.DB_ROOT_USER ?? 'root',
  password: process.env.DB_ROOT_PASSWORD ?? 'perfroot',
  database: process.env.DB_NAME ?? 'proconnect',
};

export async function resolveTargets(conn) {
  // Viewer del feed: usuario 1 (siempre existe en el dataset).
  const viewerId = 1;

  // Perfil: usuario "típico" (nº de posts más cercano a la media). El código
  // actual responde 500 si el usuario no tiene posts o si alguno de sus posts no
  // tiene comentarios (NPE en UserService.findUsersWithProfilesAndPosts), así que
  // el usuario elegido debe tener todos sus posts comentados. También se cuenta
  // cuántos usuarios caen en ese bug.
  const [[avg]] = await conn.query(
    'SELECT COUNT(*) / (SELECT COUNT(*) FROM `user`) AS avg_posts FROM posts',
  );
  const [profileRows] = await conn.query(
    `SELECT p.user_id, COUNT(DISTINCT p.id) AS posts, COUNT(c.id) AS comment_rows,
            SUM(c.id IS NULL) AS uncommented_posts
       FROM posts p LEFT JOIN comments c ON c.post_id = p.id
      GROUP BY p.user_id
     HAVING uncommented_posts = 0
      ORDER BY ABS(COUNT(DISTINCT p.id) - ?) ASC, p.user_id ASC
      LIMIT 1`,
    [Number(avg.avg_posts)],
  );
  const [[broken]] = await conn.query(
    `SELECT COUNT(*) AS users_profile_500 FROM \`user\` u
      WHERE NOT EXISTS (SELECT 1 FROM posts p WHERE p.user_id = u.id)
         OR EXISTS (SELECT 1 FROM posts p WHERE p.user_id = u.id
                     AND NOT EXISTS (SELECT 1 FROM comments c WHERE c.post_id = p.id))`,
  );

  // Comentarios: el post con más comentarios (peor caso realista).
  const [[commentPost]] = await conn.query(
    'SELECT post_id, COUNT(*) AS n FROM comments GROUP BY post_id ORDER BY n DESC, post_id ASC LIMIT 1',
  );

  // Like: post más reciente sin likes de los usuarios 1..64 (uno por VU de k6),
  // para que create+delete no colisionen con likes sembrados.
  const [[likePost]] = await conn.query(
    `SELECT p.id FROM posts p
      WHERE NOT EXISTS (SELECT 1 FROM likes l WHERE l.post_id = p.id AND l.user_id <= 64)
      ORDER BY p.id DESC LIMIT 1`,
  );

  // Feed paginado (fase 2): cursor de una página profunda (tras el post nº FEED_DEEP_OFFSET
  // en orden created_at DESC, id DESC). Mismo formato opaco que PostsService:
  // base64url("<epochMicros>:<id>"). Antes de la fase 2 el feed no pagina y se ignora.
  const deepOffset = Number(process.env.FEED_DEEP_OFFSET ?? 5000);
  const [cols] = await conn.query(
    "SELECT COLUMN_NAME c FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'posts'",
  );
  const dateCol = cols.some((c) => c.c === 'created_at') ? 'created_at' : 'date';
  await conn.query("SET time_zone = '+00:00'");
  const [[deep]] = await conn.query(
    `SELECT id, CAST(ROUND(UNIX_TIMESTAMP(${dateCol}) * 1000000) AS UNSIGNED) AS micros
       FROM posts ORDER BY ${dateCol} DESC, id DESC LIMIT 1 OFFSET ?`,
    [deepOffset],
  );
  const feedDeepCursor = deep
    ? Buffer.from(`${deep.micros}:${deep.id}`).toString('base64url')
    : null;

  return {
    viewerId,
    viewerEmail: `perf.user${viewerId}@proconnect.test`,
    feedDeepOffset: deepOffset,
    feedDeepCursor,
    profileUserId: Number(profileRows[0].user_id),
    profileUserPosts: Number(profileRows[0].posts),
    profileUserCommentRows: Number(profileRows[0].comment_rows),
    usersWithProfile500: Number(broken.users_profile_500),
    commentsPostId: Number(commentPost.post_id),
    commentsPostCount: Number(commentPost.n),
    likePostId: Number(likePost.id),
    likeUserId: 1,
  };
}

// Toda la API exige sesión desde fix-critical-risks: login con un usuario sembrado
// (password común del dataset) y devuelve la cabecera Cookie con pc_session.
export const PERF_PASSWORD = process.env.PERF_PASSWORD ?? 'PerfPass#2026';
export async function login(api, email) {
  const res = await fetch(`${api}/api/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password: PERF_PASSWORD }),
  });
  if (res.status !== 200) throw new Error(`login ${email} → HTTP ${res.status}`);
  const cookie = (res.headers.getSetCookie?.() ?? [res.headers.get('set-cookie')])
    .map((c) => c?.split(';')[0])
    .find((c) => c?.startsWith('pc_session='));
  if (!cookie) throw new Error(`login ${email}: sin cookie pc_session`);
  return cookie;
}

if (import.meta.url === `file://${process.argv[1]}`) {
  const conn = await mysql.createConnection(dbConfig);
  console.log(JSON.stringify(await resolveTargets(conn), null, 2));
  await conn.end();
}
