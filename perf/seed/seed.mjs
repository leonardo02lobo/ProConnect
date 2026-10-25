#!/usr/bin/env node
// Generador determinista del dataset de referencia de rendimiento.
//
// - Semilla fija (PERF_SEED, por defecto 20261024): dos ejecuciones sobre bases
//   vacías producen exactamente los mismos registros (ids explícitos, fechas
//   calculadas desde una fecha de referencia fija, nunca desde "now").
// - Inserta directo en MySQL por lotes, respetando el esquema de la versión bajo
//   prueba: user, profile_photos, posts, likes, comments, friends (snake_case).
// - El esquema debe existir: arranca la API una vez sobre la base vacía para que
//   lo cree (Hibernate ddl-auto=update hasta la fase 1; Flyway desde la fase 2).
//   El script NO crea tablas: detecta las columnas en information_schema y se
//   adapta (date → created_at, profile_photos → user.avatar_url/cover_url,
//   contadores posts.like_count/comment_count). La secuencia de faker es la misma
//   en todas las versiones, así que el dataset lógico es idéntico.
//
// Uso:
//   node seed.mjs                 # vacía las tablas y siembra
//   node seed.mjs --checksum-only # solo imprime conteos + checksums
//
// Variables: DB_HOST (127.0.0.1) DB_PORT (23306) DB_USER (root)
//            DB_PASSWORD (perfroot) DB_NAME (proconnect) PERF_SEED (20261024)

import { faker } from '@faker-js/faker';
import bcrypt from 'bcryptjs';
import mysql from 'mysql2/promise';

const SEED = Number(process.env.PERF_SEED ?? 20261024);
const SIZES = {
  users: 1000,
  posts: 10000,
  likes: 50000,
  comments: 30000,
  friends: 5000,
};
const PROFILE_PHOTO_RATIO = 0.9; // 90 % de usuarios con fila en profile_photos
const WALL_PHOTO_RATIO = 0.6;
const POST_IMAGE_RATIO = 0.3;
const BATCH = 1000;

// Credenciales de prueba conocidas para todos los usuarios sembrados.
export const PERF_PASSWORD = 'PerfPass#2026';
// Salt BCrypt fijo => hash idéntico en cada ejecución (determinismo). Cost 12,
// el mismo que usa SecurityConfig (BCryptPasswordEncoder(12)).
const FIXED_SALT = '$2a$12$ProConnectPerfSeedSalt';

// Fecha de referencia fija: el post 1 es el más antiguo; cada post siguiente
// es posterior (gap aleatorio), como en un feed real.
const REF_START = Date.UTC(2025, 0, 1, 0, 0, 0);

const db = {
  host: process.env.DB_HOST ?? '127.0.0.1',
  port: Number(process.env.DB_PORT ?? 23306),
  user: process.env.DB_USER ?? 'root',
  password: process.env.DB_PASSWORD ?? 'perfroot',
  database: process.env.DB_NAME ?? 'proconnect',
};

const TABLES = ['comments', 'likes', 'friends', 'posts', 'profile_photos', 'user'];

// Esquema real de la versión bajo prueba (columnas por tabla).
async function detectSchema(conn) {
  const [rows] = await conn.query(
    'SELECT TABLE_NAME t, COLUMN_NAME c FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = ?',
    [db.database],
  );
  const cols = {};
  for (const r of rows) (cols[r.t] ??= new Set()).add(r.c);
  const has = (t, c) => cols[t]?.has(c) ?? false;
  const dateCol = (t) => (has(t, 'created_at') ? 'created_at' : 'date');
  return {
    tables: TABLES.filter((t) => cols[t]),
    photosTable: Boolean(cols.profile_photos),
    userMedia: has('user', 'avatar_url'),
    counters: has('posts', 'like_count'),
    postsDate: dateCol('posts'),
    likesDate: dateCol('likes'),
    commentsDate: dateCol('comments'),
  };
}

const clip = (s, n) => (s.length > n ? s.slice(0, n) : s);
const toDate = (ms) => new Date(ms);

function generate() {
  faker.seed(SEED);
  const passwordHash = bcrypt.hashSync(PERF_PASSWORD, FIXED_SALT);

  // ---- users (name, user_name, email son UNIQUE en el esquema actual)
  const users = [];
  const usedNames = new Set();
  for (let id = 1; id <= SIZES.users; id++) {
    const first = faker.person.firstName();
    const last = faker.person.lastName();
    let name = `${first} ${last}`;
    if (usedNames.has(name)) name = `${name} ${id}`;
    usedNames.add(name);
    const userName = clip(`${first}.${last}`.toLowerCase().replace(/[^a-z0-9.]/g, ''), 40) + `.${id}`;
    users.push([id, `perf.user${id}@proconnect.test`, name, passwordHash, 'USER', userName]);
  }

  // ---- profile_photos (1:1 opcional)
  const photos = [];
  let photoId = 1;
  for (let uid = 1; uid <= SIZES.users; uid++) {
    if (faker.datatype.boolean(PROFILE_PHOTO_RATIO)) {
      const wall = faker.datatype.boolean(WALL_PHOTO_RATIO)
        ? `/upload/uploads/wall-${uid}.jpg`
        : null;
      photos.push([photoId++, `/upload/uploads/avatar-${uid}.jpg`, uid, wall]);
    }
  }

  // ---- posts (fechas crecientes desde REF_START)
  const posts = [];
  const postDates = new Array(SIZES.posts + 1);
  let t = REF_START;
  for (let id = 1; id <= SIZES.posts; id++) {
    t += faker.number.int({ min: 60_000, max: 90 * 60_000 }); // 1–90 min
    postDates[id] = t;
    const userId = faker.number.int({ min: 1, max: SIZES.users });
    const title = clip(faker.lorem.sentence({ min: 3, max: 8 }), 120);
    const content = clip(faker.lorem.paragraph({ min: 1, max: 3 }), 250);
    const imageUrl = faker.datatype.boolean(POST_IMAGE_RATIO) ? `/upload/uploads/post-${id}.jpg` : null;
    posts.push([id, content, toDate(t), imageUrl, title, userId]);
  }
  const lastPostTs = t;

  // ---- likes (pares user/post únicos, aunque el esquema actual no lo exige)
  const likes = [];
  const likePairs = new Set();
  while (likes.length < SIZES.likes) {
    const userId = faker.number.int({ min: 1, max: SIZES.users });
    const postId = faker.number.int({ min: 1, max: SIZES.posts });
    const key = userId * 100_000 + postId;
    if (likePairs.has(key)) continue;
    likePairs.add(key);
    const date = postDates[postId] + faker.number.int({ min: 1_000, max: 3 * 86_400_000 });
    likes.push([likes.length + 1, toDate(date), postId, userId]);
  }

  // ---- comments (distribución aleatoria: hay posts sin comentarios, como en la realidad)
  const comments = [];
  for (let id = 1; id <= SIZES.comments; id++) {
    const postId = faker.number.int({ min: 1, max: SIZES.posts });
    const userId = faker.number.int({ min: 1, max: SIZES.users });
    const date = postDates[postId] + faker.number.int({ min: 1_000, max: 3 * 86_400_000 });
    comments.push([id, clip(faker.lorem.sentence({ min: 4, max: 16 }), 250), toDate(date), postId, userId]);
  }

  // ---- friends (pares dirigidos únicos, sin auto-amistad)
  const friends = [];
  const friendPairs = new Set();
  while (friends.length < SIZES.friends) {
    const follow = faker.number.int({ min: 1, max: SIZES.users });
    const followed = faker.number.int({ min: 1, max: SIZES.users });
    if (follow === followed) continue;
    const key = follow * 10_000 + followed;
    if (friendPairs.has(key)) continue;
    friendPairs.add(key);
    const status = faker.helpers.weightedArrayElement([
      { weight: 7, value: 'ACCEPTED' },
      { weight: 2, value: 'PENDING' },
      { weight: 1, value: 'REJECTED' },
    ]);
    friends.push([friends.length + 1, status, followed, follow]);
  }

  return { users, photos, posts, likes, comments, friends, lastPostTs };
}

async function insertBatches(conn, table, columns, rows) {
  const cols = columns.map((c) => `\`${c}\``).join(', ');
  for (let i = 0; i < rows.length; i += BATCH) {
    const chunk = rows.slice(i, i + BATCH);
    await conn.query(`INSERT INTO \`${table}\` (${cols}) VALUES ?`, [chunk]);
  }
  process.stdout.write(`  ${table.padEnd(15)} ${rows.length}\n`);
}

// Checksum por tabla independiente del orden físico: sirve para comprobar que
// dos ejecuciones con la misma semilla dejan exactamente los mismos registros.
// Mismas expresiones que en la línea base: con el mismo dataset lógico dan el
// mismo checksum aunque la columna de fecha se llame `created_at`. Sin la tabla
// profile_photos (fase 2, V5) se calcula `user_media` sobre user.avatar_url/cover_url.
function checksumSql(sc) {
  const sql = {
    user: "SELECT COUNT(*) n, BIT_XOR(CRC32(CONCAT_WS('|',id,email,name,password,role,user_name))) c FROM `user`",
    posts: `SELECT COUNT(*) n, BIT_XOR(CRC32(CONCAT_WS('|',id,content,${sc.postsDate},IFNULL(image_url,'-'),title,user_id))) c FROM posts`,
    likes: `SELECT COUNT(*) n, BIT_XOR(CRC32(CONCAT_WS('|',id,${sc.likesDate},post_id,user_id))) c FROM likes`,
    comments: `SELECT COUNT(*) n, BIT_XOR(CRC32(CONCAT_WS('|',id,content,${sc.commentsDate},post_id,user_id))) c FROM comments`,
    friends: "SELECT COUNT(*) n, BIT_XOR(CRC32(CONCAT_WS('|',id,status,user_id_be_followed,user_id_follow))) c FROM friends",
  };
  if (sc.photosTable) {
    sql.profile_photos =
      "SELECT COUNT(*) n, BIT_XOR(CRC32(CONCAT_WS('|',id,photo_url,user_id,IFNULL(wall_photo_url,'-')))) c FROM profile_photos";
  }
  if (sc.userMedia) {
    sql.user_media =
      "SELECT COUNT(*) n, BIT_XOR(CRC32(CONCAT_WS('|',id,avatar_url,IFNULL(cover_url,'-')))) c FROM `user` WHERE avatar_url IS NOT NULL";
  }
  if (sc.counters) {
    sql.post_counters =
      'SELECT COUNT(*) n, BIT_XOR(CRC32(CONCAT_WS(\'|\',id,like_count,comment_count))) c FROM posts';
  }
  return sql;
}

async function checksums(conn, sc) {
  const out = {};
  for (const [table, sql] of Object.entries(checksumSql(sc))) {
    const [[row]] = await conn.query(sql);
    out[table] = { rows: Number(row.n), checksum: String(row.c) };
  }
  return out;
}

async function main() {
  const conn = await mysql.createConnection({ ...db, timezone: 'Z', multipleStatements: false });
  try {
    const sc = await detectSchema(conn);
    if (!process.argv.includes('--checksum-only')) {
      const t0 = Date.now();
      console.log(`Generando dataset (seed=${SEED})...`);
      const data = generate();
      console.log(`Vaciando tablas en ${db.host}:${db.port}/${db.database}...`);
      await conn.query('SET FOREIGN_KEY_CHECKS = 0');
      for (const table of sc.tables) await conn.query(`TRUNCATE TABLE \`${table}\``);
      await conn.query('SET FOREIGN_KEY_CHECKS = 1');
      console.log('Insertando:');
      await conn.beginTransaction();
      if (sc.userMedia) {
        // Fase 2 (V3): avatar y portada viven en `user`; mismos valores que profile_photos.
        const media = new Map(data.photos.map(([, photo, uid, wall]) => [uid, [photo, wall]]));
        const users = data.users.map((u) => [...u, ...(media.get(u[0]) ?? [null, null])]);
        await insertBatches(conn, 'user', ['id', 'email', 'name', 'password', 'role', 'user_name', 'avatar_url', 'cover_url'], users);
      } else {
        await insertBatches(conn, 'user', ['id', 'email', 'name', 'password', 'role', 'user_name'], data.users);
      }
      if (sc.photosTable) {
        await insertBatches(conn, 'profile_photos', ['id', 'photo_url', 'user_id', 'wall_photo_url'], data.photos);
      }
      await insertBatches(conn, 'posts', ['id', 'content', sc.postsDate, 'image_url', 'title', 'user_id'], data.posts);
      await insertBatches(conn, 'likes', ['id', sc.likesDate, 'post_id', 'user_id'], data.likes);
      await insertBatches(conn, 'comments', ['id', 'content', sc.commentsDate, 'post_id', 'user_id'], data.comments);
      await insertBatches(conn, 'friends', ['id', 'status', 'user_id_be_followed', 'user_id_follow'], data.friends);
      if (sc.counters) {
        // Fase 2 (V4): contadores denormalizados, igual que el backfill de la migración.
        await conn.query(`UPDATE posts p
            LEFT JOIN (SELECT post_id, COUNT(*) n FROM likes GROUP BY post_id) l ON l.post_id = p.id
            LEFT JOIN (SELECT post_id, COUNT(*) n FROM comments GROUP BY post_id) c ON c.post_id = p.id
            SET p.like_count = IFNULL(l.n, 0), p.comment_count = IFNULL(c.n, 0)`);
        process.stdout.write('  posts.like_count/comment_count recalculados\n');
      }
      await conn.commit();
      await conn.query(`ANALYZE TABLE ${sc.tables.map((x) => `\`${x}\``).join(', ')}`);
      console.log(`Listo en ${((Date.now() - t0) / 1000).toFixed(1)} s. Password de todos los usuarios: ${PERF_PASSWORD}`);
    }
    console.log(JSON.stringify({ seed: SEED, tables: await checksums(conn, sc) }, null, 2));
  } finally {
    await conn.end();
  }
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
