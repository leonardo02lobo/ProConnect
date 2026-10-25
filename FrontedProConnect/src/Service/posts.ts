import type { CursorPage, FeedPostDTO, PostDTO } from "../types/Dtos";
import { apiRequest } from "./http";

export interface PostInput {
    title: string;
    content: string;
    imageUrl?: string | null;
}

/** Query string de paginación por cursor. */
export function cursorQuery(cursor?: string | null, limit?: number): string {
    const params = new URLSearchParams();
    if (limit) params.set("limit", String(limit));
    if (cursor) params.set("cursor", cursor);
    const query = params.toString();
    return query ? `?${query}` : "";
}

// El autor sale de la sesión (cookie); nunca se envía userId.
export const postsService = {
    /** Una página del feed (20 por defecto); pasar el nextCursor anterior para la siguiente. */
    feed: (cursor?: string | null): Promise<CursorPage<FeedPostDTO>> =>
        apiRequest<CursorPage<FeedPostDTO>>(`/api/posts${cursorQuery(cursor)}`),

    get: (id: number): Promise<PostDTO> => apiRequest<PostDTO>(`/api/posts/${id}`),

    create: (post: PostInput): Promise<PostDTO> => apiRequest<PostDTO>("/api/posts", "POST", post),

    update: (id: number, post: PostInput): Promise<PostDTO> => apiRequest<PostDTO>(`/api/posts/${id}`, "PUT", post),

    remove: (id: number): Promise<void> => apiRequest<void>(`/api/posts/${id}`, "DELETE"),
};
