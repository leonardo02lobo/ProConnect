import type { CommentDTO } from "../types/Dtos";
import { apiRequest } from "./http";

export const commentsService = {
    list: (postId: number): Promise<CommentDTO[]> => apiRequest<CommentDTO[]>(`/api/posts/${postId}/comments`),

    create: (postId: number, content: string): Promise<CommentDTO> =>
        apiRequest<CommentDTO>(`/api/posts/${postId}/comments`, "POST", { content }),

    remove: (commentId: number): Promise<void> => apiRequest<void>(`/api/comments/${commentId}`, "DELETE"),
};
