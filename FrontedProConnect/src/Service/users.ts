import type { UserDTO, UserProfileDTO } from "../types/Dtos";
import { apiRequest } from "./http";

export const usersService = {
    profile: (id: number): Promise<UserProfileDTO> => apiRequest<UserProfileDTO>(`/api/users/${id}/profile`),

    search: (query: string): Promise<UserDTO[]> =>
        apiRequest<UserDTO[]>(`/api/users?q=${encodeURIComponent(query)}`),
};
