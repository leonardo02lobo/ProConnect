import type { FeedPostDTO, PostDTO } from "../types/Dtos";
import { apiRequest } from "./http";

export interface PostInput {
    title: string;
    content: string;
    imageUrl?: string | null;
}

// El autor sale de la sesión (cookie); nunca se envía userId.
export const postsService = {
    feed: (): Promise<FeedPostDTO[]> => apiRequest<FeedPostDTO[]>("/api/posts"),

    get: (id: number): Promise<PostDTO> => apiRequest<PostDTO>(`/api/posts/${id}`),

    create: (post: PostInput): Promise<PostDTO> => apiRequest<PostDTO>("/api/posts", "POST", post),

    update: (id: number, post: PostInput): Promise<PostDTO> => apiRequest<PostDTO>(`/api/posts/${id}`, "PUT", post),

    remove: (id: number): Promise<void> => apiRequest<void>(`/api/posts/${id}`, "DELETE"),
};
