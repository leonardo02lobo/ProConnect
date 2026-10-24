import { atom } from "nanostores";
import type { UserDTO } from "../types/Dtos";
import { AuthApi } from "./AuthApi";

/** Usuario de la sesión actual (cargado desde GET /api/auth/me). undefined = aún no cargado. */
export const $user = atom<UserDTO | null | undefined>(undefined);

let loading: Promise<UserDTO | null> | null = null;

export function loadSession(force = false): Promise<UserDTO | null> {
    if (!force && $user.get() !== undefined) {
        return Promise.resolve($user.get() ?? null);
    }
    if (!loading || force) {
        loading = AuthApi.Me()
            .then(async (response) => (response.ok ? ((await response.json()) as UserDTO) : null))
            .catch(() => null)
            .then((user) => {
                $user.set(user);
                loading = null;
                return user;
            });
    }
    return loading;
}

export async function logout(): Promise<void> {
    try {
        await AuthApi.Logout();
    } finally {
        $user.set(null);
        window.location.href = "/login";
    }
}
