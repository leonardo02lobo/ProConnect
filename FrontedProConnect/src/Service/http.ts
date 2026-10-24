// Cliente HTTP único del frontend. Toda llamada al backend pasa por aquí.

/** URL pública del gateway (lo que ve el navegador). */
export const API_URL: string = (import.meta.env.PUBLIC_API_URL ?? "").replace(/\/+$/, "");

export const LOGIN_PATH = "/login";

export interface ApiFetchOptions extends RequestInit {
    /** Si es false, un 401 no redirige al login (p. ej. en el propio formulario de login). */
    redirectOn401?: boolean;
}

/** fetch contra el gateway con la cookie de sesión (`credentials: "include"`). */
export async function apiFetch(path: string, options: ApiFetchOptions = {}): Promise<Response> {
    const { redirectOn401 = true, ...init } = options;
    const url = path.startsWith("http") ? path : `${API_URL}${path.startsWith("/") ? "" : "/"}${path}`;
    const response = await fetch(url, { ...init, credentials: "include" });
    if (response.status === 401 && redirectOn401 && typeof window !== "undefined"
        && window.location.pathname !== LOGIN_PATH) {
        window.location.href = LOGIN_PATH;
    }
    return response;
}

/** apiFetch con body JSON. */
export function apiJson(path: string, method: string, body?: unknown, options: ApiFetchOptions = {}): Promise<Response> {
    return apiFetch(path, {
        ...options,
        method,
        headers: { "Content-Type": "application/json", ...(options.headers ?? {}) },
        body: body === undefined ? undefined : JSON.stringify(body),
    });
}

/** URL de una imagen servida por el servicio de media a través del gateway. */
export function mediaUrl(fileName?: string | null): string | undefined {
    if (!fileName) {
        return undefined;
    }
    if (/^https?:\/\//.test(fileName)) {
        return fileName;
    }
    return `${API_URL}/upload/uploads/${fileName.replace(/^\/+/, "")}`;
}
