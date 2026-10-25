import { apiJson } from "./http";

// El usuario del like sale de la sesión (cookie), no se envía userId.
export const apis = () => {
    async function AddLike(postId: number) {
        const response = await apiJson("/api/like/create", "POST", { postId });
        if (!response.ok) {
            const errorText = await response.text();
            console.error("Error del servidor:", errorText);
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.text();
    }

    async function RemoveLike(postId: number) {
        const response = await apiJson("/api/like/delete", "DELETE", { postId });
        if (!response.ok) {
            const errorText = await response.text();
            console.error("Error del servidor:", errorText);
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.text();
    }

    return { AddLike, RemoveLike }
}
