// Cliente HTTP único del frontend. Toda llamada al backend pasa por aquí (los componentes usan los servicios
// por recurso de esta carpeta, nunca fetch directo).
import type { ProblemDetail } from "../types/Dtos";

/** URL pública del gateway (lo que ve el navegador). */
export const API_URL: string = (import.meta.env.PUBLIC_API_URL ?? "").replace(/\/+$/, "");

export const LOGIN_PATH = "/login";

export interface ApiFetchOptions extends RequestInit {
    /** Si es false, un 401 no redirige al login (p. ej. en el propio formulario de login). */
    redirectOn401?: boolean;
}

/** Error HTTP de la API con el cuerpo problem+json (RFC 7807) si lo hay. */
export class ApiError extends Error {
    constructor(
        readonly status: number,
        readonly problem: ProblemDetail | null,
    ) {
        super(problem?.detail ?? `HTTP ${status}`);
        this.name = "ApiError";
    }
}

/** fetch contra el gateway con la cookie de sesión (`credentials: "include"`). */
export async function apiFetch(path: string, options: ApiFetchOptions = {}): Promise<Response> {
    const { redirectOn401 = true, ...init } = options;
    const url = path.startsWith("http") ? path : `${API_URL}${path.startsWith("/") ? "" : "/"}${path}`;
    const response = await fetch(url, { ...init, credentials: "include" });
    if (
        response.status === 401 &&
        redirectOn401 &&
        typeof window !== "undefined" &&
        window.location.pathname !== LOGIN_PATH
    ) {
        window.location.href = LOGIN_PATH;
    }
    return response;
}

async function readProblem(response: Response): Promise<ProblemDetail | null> {
    try {
        return (await response.json()) as ProblemDetail;
    } catch {
        return null;
    }
}

/**
 * Petición JSON tipada: devuelve el cuerpo parseado (undefined en 204) o lanza {@link ApiError}.
 */
export async function apiRequest<T>(
    path: string,
    method = "GET",
    body?: unknown,
    options: ApiFetchOptions = {},
): Promise<T> {
    const headers = new Headers(options.headers);
    if (body !== undefined && !(body instanceof FormData)) {
        headers.set("Content-Type", "application/json");
    }
    const response = await apiFetch(path, {
        ...options,
        method,
        headers,
        body: body === undefined ? undefined : body instanceof FormData ? body : JSON.stringify(body),
    });
    if (!response.ok) {
        throw new ApiError(response.status, await readProblem(response));
    }
    if (response.status === 204) {
        return undefined as T;
    }
    const text = await response.text();
    return (text ? JSON.parse(text) : undefined) as T;
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

/** Avatar por defecto servido localmente (sin CDN de terceros: lo exige la CSP). */
export const DEFAULT_AVATAR = "/assets/default-avatar.svg";

export function avatarUrl(fileName?: string | null): string {
    return mediaUrl(fileName) ?? DEFAULT_AVATAR;
}
