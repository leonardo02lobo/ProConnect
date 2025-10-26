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
    email: string;
    name: string;
    password: string;
    role: string;
    userName: string;
    photoUrl: string;
    wallPhotoUrl: string;
    posts: PostDTO[];
}

export interface CommentsDTO {
    id: number;
    postId: number;
    userId: number;
    content: string;
    date: Date;
    user: UserDTO;
}

export interface UserDTO {
    id: number;
    email: string;
    name: string;
    password: string;
    role: string;
    userName: string;
    profilePhotosDto: ProfilePhotoDto;
}

export interface ProfilePhotoDto {
    Id: number;
    userId: number;
    photoUrl: string;
    wallPhotoUrl: string;
}