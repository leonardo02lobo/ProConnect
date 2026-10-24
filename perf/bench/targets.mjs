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

  return {
    viewerId,
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

if (import.meta.url === `file://${process.argv[1]}`) {
  const conn = await mysql.createConnection(dbConfig);
  console.log(JSON.stringify(await resolveTargets(conn), null, 2));
  await conn.end();
}
