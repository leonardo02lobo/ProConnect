import { apiFetch, apiJson } from "./http";

export interface RegisterData {
    name: string;
    userName: string;
    email: string;
    password: string;
}

export const AuthApi = {
    Login: (email: string, password: string): Promise<Response> =>
        apiJson("/api/auth/login", "POST", { email, password }, { redirectOn401: false }),

    Register: (data: RegisterData): Promise<Response> =>
        apiJson("/api/auth/register", "POST", data, { redirectOn401: false }),

    Me: (): Promise<Response> => apiFetch("/api/auth/me"),

    Logout: (): Promise<Response> => apiFetch("/api/auth/logout", { method: "POST", redirectOn401: false }),
}
