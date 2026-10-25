import { apiFetch, apiJson } from "./http";

export const PostsApi = {

    // El autor sale de la sesión (cookie); no se envía userId.
    Create: (content: string, title: string, imageUrl: string): Promise<Response> =>
        apiJson("/api/posts/create", "POST", {
            content: content,
            title: title,
            imageUrl: imageUrl,
            totalLikes: 0,
            totalComentarios: 0,
            userLiked: false
        }),

    Posts: (): Promise<Response> => apiFetch("/api/posts"),

    Comment: (postId: number, content: string): Promise<Response> =>
        apiJson("/api/comment/create", "POST", { postId, content }),

    UploadImage: (file: File): Promise<Response> => {
        const formData = new FormData();
        formData.append("miArchivo", file);
        return apiFetch("/upload/subirImagen", { method: "POST", body: formData });
    },
}
