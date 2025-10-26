import { getUserFromCookie } from "./GetCookie";

const URL_BASE = "http://localhost:8080/api/posts"

export const PostsApi = {

    Create: (content: string, title: string, imageUrl: string): Promise<Response> => {
        const user = getUserFromCookie();
        if (!user || !user.id) {
            return Promise.reject(new Error('No se encontró la cookie de usuario'));
        }
        return fetch(`${URL_BASE}/create`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                userId: user.id,
                content: content,
                title: title,
                imageUrl: imageUrl,
                totalLikes: 0,
                totalComentarios: 0,
                userLiked: false
            })
        })
            .then((response) => {
                return response;
            });
    },
    Posts: (): Promise<Response> => {
        const user = getUserFromCookie();
        if (!user || !user.id) {
            return Promise.reject(new Error('No se encontró la cookie de usuario'));
        }

        return fetch(`${URL_BASE}/${user.id}`)
            .then((response) => {
                return response;
            });
    }
}