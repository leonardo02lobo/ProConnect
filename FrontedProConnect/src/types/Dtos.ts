export interface PostDTO {
    id: number;
    title: string;
    content: string;
    user?: UserDTO;
    date: string;
    imageUrl: string;
    totalLikes: number;
    totalComentarios: number;
    userLiked?: boolean;
    comments: CommentsDTO[];
}


export interface UserProfileDTO {
    id: number;
    /** Solo presente para el propio usuario o un ADMIN (verify-web-security). */
    email?: string;
    name: string;
    role: string;
    userName: string;
    photoUrl: string | null;
    wallPhotoUrl: string | null;
    posts: PostDTO[];
}

export interface CommentsDTO {
    id: number;
    postId: number;
    userId: number;
    content: string;
    date: Date;
    user: UserDTO | null;
}

/** Usuario público (UserPublicDto del backend). Nunca incluye password. */
export interface UserDTO {
    id: number;
    /** Solo presente para el propio usuario o un ADMIN (verify-web-security). */
    email?: string;
    name: string;
    role: "USER" | "COMPANY_ADMIN" | "ADMIN" | string;
    userName: string;
    photoUrl: string | null;
    wallPhotoUrl: string | null;
}
