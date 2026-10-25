import type { LikeDTO } from "../types/Dtos";
import { apiRequest } from "./http";

// El usuario del like sale de la sesión; DELETE sin body.
export const likesService = {
    like: (postId: number): Promise<LikeDTO> => apiRequest<LikeDTO>(`/api/posts/${postId}/likes`, "POST"),

    unlike: (postId: number): Promise<void> => apiRequest<void>(`/api/posts/${postId}/likes`, "DELETE"),
};
