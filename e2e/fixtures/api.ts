import fs from 'node:fs';
import path from 'node:path';
import { request, type APIRequestContext, type APIResponse } from '@playwright/test';
import { API_URL } from './env';

/**
 * Thin API client over the gateway. It talks to the routes that are valid while
 * `fix-critical-risks` is the base (CONTRACT.md · "Rutas API vigentes"). If
 * `cleanup-codebase` renames routes, only this file should need changes.
 */

export interface PublicUser {
  id: number;
  name: string;
  userName: string;
  email: string;
  role?: string;
  photoUrl?: string | null;
  wallPhotoUrl?: string | null;
}

export interface FeedPost {
  id: number;
  title: string;
  content: string;
  imageUrl: string | null;
  totalLikes: number;
  totalComentarios: number;
  userLiked?: boolean;
  user?: { id: number; name: string };
  comments: Array<{ id: number; content: string; user?: { id: number; name: string } }>;
}

export const TEST_IMAGE = path.join(__dirname, '..', 'assets', 'test-image.png');

async function ensureOk(res: APIResponse, what: string): Promise<APIResponse> {
  if (!res.ok()) {
    throw new Error(`${what} failed: HTTP ${res.status()} ${res.url()} → ${(await res.text()).slice(0, 300)}`);
  }
  return res;
}

async function jsonOrNull<T>(res: APIResponse): Promise<T | null> {
  const body = await res.text();
  if (!body.trim()) return null;
  return JSON.parse(body) as T;
}

export class ApiClient {
  constructor(readonly ctx: APIRequestContext) {}

  /** New, cookie-less context pointing at the gateway. */
  static async create(storageState?: string): Promise<ApiClient> {
    const ctx = await request.newContext({ baseURL: API_URL, storageState });
    return new ApiClient(ctx);
  }

  dispose() {
    return this.ctx.dispose();
  }

  /** POST /api/auth/register. Returns the HTTP status (201/200 created, 409 duplicate). */
  async register(user: { name: string; userName: string; email: string; password: string }): Promise<number> {
    const res = await this.ctx.post('/api/auth/register', { data: user });
    return res.status();
  }

  /** POST /api/auth/login. Stores the `pc_session` cookie in this context. */
  async login(email: string, password: string): Promise<PublicUser> {
    const res = await ensureOk(await this.ctx.post('/api/auth/login', { data: { email, password } }), 'login');
    return (await res.json()) as PublicUser;
  }

  async loginRaw(email: string, password: string): Promise<APIResponse> {
    return this.ctx.post('/api/auth/login', { data: { email, password } });
  }

  async me(): Promise<PublicUser> {
    const res = await ensureOk(await this.ctx.get('/api/auth/me'), 'GET /api/auth/me');
    return (await res.json()) as PublicUser;
  }

  async logout(): Promise<void> {
    await this.ctx.post('/api/auth/logout');
  }

  /** Uploads an image through the gateway media route; returns the stored filename. */
  async uploadImage(file = TEST_IMAGE): Promise<string> {
    const res = await ensureOk(
      await this.ctx.post('/upload/subirImagen', {
        multipart: {
          miArchivo: { name: path.basename(file), mimeType: 'image/png', buffer: fs.readFileSync(file) },
        },
      }),
      'upload image',
    );
    const body = (await res.json()) as { data?: { filename?: string } };
    if (!body.data?.filename) throw new Error(`upload returned no filename: ${JSON.stringify(body)}`);
    return body.data.filename;
  }

  /** userId is ignored by the server after fix-critical-risks (actor comes from the token). */
  async createPost(post: { title: string; content: string; imageUrl?: string }, userId?: number): Promise<void> {
    await ensureOk(
      await this.ctx.post('/api/posts/create', {
        data: { userId, title: post.title, content: post.content, imageUrl: post.imageUrl ?? '' },
      }),
      'create post',
    );
  }

  /** Feed as seen by the given viewer (userLiked depends on the viewer). */
  async feed(viewerId: number): Promise<FeedPost[]> {
    let res = await this.ctx.get(`/api/posts/${viewerId}`);
    if (res.status() === 404 || res.status() === 405) res = await this.ctx.get('/api/posts');
    await ensureOk(res, 'GET feed');
    return (await jsonOrNull<FeedPost[]>(res)) ?? [];
  }

  async findPostByTitle(viewerId: number, title: string): Promise<FeedPost | undefined> {
    return (await this.feed(viewerId)).find((p) => p.title === title);
  }

  async like(postId: number, userId?: number): Promise<void> {
    await ensureOk(await this.ctx.post('/api/like/create', { data: { postId, userId } }), 'like');
  }

  async unlike(postId: number, userId?: number): Promise<void> {
    await ensureOk(await this.ctx.delete('/api/like/delete', { data: { postId, userId } }), 'unlike');
  }

  async comment(postId: number, content: string, userId?: number): Promise<void> {
    await ensureOk(await this.ctx.post('/api/comment/create', { data: { postId, content, userId } }), 'comment');
  }
}
