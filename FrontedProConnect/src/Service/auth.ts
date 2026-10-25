import type { UserDTO } from "../types/Dtos";
import { apiRequest } from "./http";

export interface RegisterData {
    name: string;
    userName: string;
    email: string;
    password: string;
}

export const authService = {
    login: (email: string, password: string): Promise<UserDTO> =>
        apiRequest<UserDTO>("/api/auth/login", "POST", { email, password }, { redirectOn401: false }),

    register: (data: RegisterData): Promise<UserDTO> =>
        apiRequest<UserDTO>("/api/auth/register", "POST", data, { redirectOn401: false }),

    me: (): Promise<UserDTO> => apiRequest<UserDTO>("/api/auth/me", "GET", undefined, { redirectOn401: false }),

    logout: (): Promise<void> => apiRequest<void>("/api/auth/logout", "POST", undefined, { redirectOn401: false }),
};
