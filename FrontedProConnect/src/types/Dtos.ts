// Tipos alineados con los DTOs del backend (backProconnect/model).

export type Role = "USER" | "COMPANY_ADMIN" | "ADMIN";

/** Usuario público (UserPublicDto). Nunca incluye password. */
export interface UserDTO {
    id: number;
    /** Solo presente para el propio usuario o un ADMIN. */
    email?: string;
    name: string;
    role: "USER" | "COMPANY_ADMIN" | "ADMIN" | string;
    userName: string;
    photoUrl: string | null;
    wallPhotoUrl: string | null;
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
    totalLikes: number;
    totalComments: number;
    userLiked: boolean;
    comments: CommentDTO[];
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
    photoUrl: string | null;
    wallPhotoUrl: string | null;
    posts: FeedPostDTO[];
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

/** Usuario en las rutas de administración (AdminUserDto). */
export interface AdminUserDTO {
    id: number;
    name: string;
    userName: string;
    email: string;
    role: Role;
}

/** Página de GET /api/admin/users (AdminUserPage). */
export interface AdminUserPageDTO {
    content: AdminUserDTO[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
}
