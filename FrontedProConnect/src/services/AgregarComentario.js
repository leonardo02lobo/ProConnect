import { getCookie } from "./GetCookie";

export async function setComentario(id) {
    const token = await getCookie();
    try {
        const response = await fetch('http://localhost:3000/api/Comentarios/CrearComentario', {
            method: "POST",
            credentials: "include",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                contenido: contenido?.value,
                publicacion_id: id,
                usuario_id: token['id']
            })
        })
        
        return response;
    } catch (e) {
        return null;
    }
}