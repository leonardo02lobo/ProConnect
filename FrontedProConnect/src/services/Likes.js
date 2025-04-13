export async function DarLikeId(id) {
    try {
        const response = await fetch(`http://localhost:3000/api/Publicacion/Publicacion/DarLike/${id}`, {
            method: "GET",
            credentials: "include",
            headers: {
                "Content-Type": "application/json",
            },
        })
        return response;
    } catch (e) {
        return null;
    }
}
export async function QuitarLikeId(id) {
    try {
        const response = await fetch(`http://localhost:3000/api/Publicacion/Publicacion/EliminarLike/${id}`, {
            method: "GET",
            credentials: "include",
            headers: {
                "Content-Type": "application/json",
            },
        })
        return response;
    } catch (e) {
        return null;
    }
}