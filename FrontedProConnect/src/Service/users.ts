import type { CursorPage, FeedPostDTO, PageResponse, UserDTO, UserProfileDTO } from "../types/Dtos";
import { apiRequest } from "./http";
import { cursorQuery } from "./posts";

export const usersService = {
    profile: (id: number): Promise<UserProfileDTO> => apiRequest<UserProfileDTO>(`/api/users/${id}/profile`),

    /** Páginas siguientes de los posts del perfil (cursor = nextCursor del perfil o de la página anterior). */
    posts: (id: number, cursor: string): Promise<CursorPage<FeedPostDTO>> =>
        apiRequest<CursorPage<FeedPostDTO>>(`/api/users/${id}/posts${cursorQuery(cursor)}`),

    search: (query: string): Promise<PageResponse<UserDTO>> =>
        apiRequest<PageResponse<UserDTO>>(`/api/users?q=${encodeURIComponent(query)}`),
};
