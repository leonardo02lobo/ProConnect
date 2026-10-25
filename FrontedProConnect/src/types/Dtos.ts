// Tipos alineados con los DTOs del backend (backProconnect/model).

/** Usuario público (UserPublicDto). Nunca incluye password. */
export interface UserDTO {
    id: number;
    /** Solo presente para el propio usuario o un ADMIN. */
    email?: string;
    name: string;
    role: "USER" | "COMPANY_ADMIN" | "ADMIN" | string;
    userName: string;
    /** Nombre de archivo del servicio de media (columna user.avatar_url). */
    avatarUrl: string | null;
    coverUrl: string | null;
}

/** Página por cursor (feed, posts de un perfil, comentarios). nextCursor = null en la última. */
export interface CursorPage<T> {
    items: T[];
    nextCursor: string | null;
}

/** Página de un listado (?page=&size=, máx. 100). */
export interface PageResponse<T> {
    items: T[];
    page: number;
    size: number;
    hasNext: boolean;
}

/** Post sin agregados (PostDto). */
export interface PostDTO {
    id: number;
    userId: number;
    title: string;
    content: string;
    imageUrl: string | null;
    date: string;
}

/** Post del feed o de un perfil (FeedPostDto). */
export interface FeedPostDTO extends PostDTO {
    user: UserDTO;
    likeCount: number;
    commentCount: number;
    userLiked: boolean;
    /** Últimos 3 comentarios (orden cronológico); la lista completa: GET /api/posts/{id}/comments. */
    commentsPreview: CommentDTO[];
}

export interface CommentDTO {
    id: number;
    postId: number;
    userId: number;
    content: string;
    date: string;
    user: UserDTO | null;
}

export interface LikeDTO {
    id: number;
    userId: number;
    postId: number;
    date: string;
}

export interface UserProfileDTO {
    id: number;
    /** Solo presente para el propio usuario o un ADMIN. */
    email?: string;
    name: string;
    role: string;
    userName: string;
    avatarUrl: string | null;
    coverUrl: string | null;
    /** Primera página de posts; las siguientes con usersService.posts(id, nextCursor). */
    posts: FeedPostDTO[];
    nextCursor: string | null;
}

/** Error de la API (application/problem+json, RFC 7807). */
export interface ProblemDetail {
    type?: string;
    title?: string;
    status: number;
    detail?: string;
    instance?: string;
    traceId?: string;
    errors?: Array<{ field: string; message: string }>;
}
