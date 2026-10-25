import type { CommentDTO, CursorPage } from "../types/Dtos";
import { apiRequest } from "./http";
import { cursorQuery } from "./posts";

export const commentsService = {
    /** Comentarios del post (más antiguo primero), paginados por cursor. */
    list: (postId: number, cursor?: string | null): Promise<CursorPage<CommentDTO>> =>
        apiRequest<CursorPage<CommentDTO>>(`/api/posts/${postId}/comments${cursorQuery(cursor)}`),

    create: (postId: number, content: string): Promise<CommentDTO> =>
        apiRequest<CommentDTO>(`/api/posts/${postId}/comments`, "POST", { content }),

    remove: (commentId: number): Promise<void> => apiRequest<void>(`/api/comments/${commentId}`, "DELETE"),
};
