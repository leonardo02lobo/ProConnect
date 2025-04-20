import type { Comentarios } from "../models/Comentarios";
import { getCookie } from "./UsuarioService";

export async function ObtenerComentarios(id: number) {
    let ComentariosPost: Comentarios[] = [];
    try {
        const response = await fetch(`http://localhost:3000/api/Comentarios/ObtenerComentariosPorPublicacion/${id}`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
            },
        });
        const data = await response.json();
        if(data === null || data.length === 0){
            return ComentariosPost
        }
        return data;
    } catch (e) {
        console.error("Error al obtener comentarios:", e);
        return ComentariosPost;
    }
}

export async function setComentario(id: number,contenido: string | undefined) {
    const token = await getCookie();
    try {
        const response = await fetch('http://localhost:3000/api/Comentarios/CrearComentario', {
            method: "POST",
            credentials: "include",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                contenido: contenido,
                publicacion_id: id,
                usuario_id: token['id']
            })
        })
        
        return response;
    } catch (e) {
        return null;
    }
}