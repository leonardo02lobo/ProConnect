import type { Comentarios } from "../models/Comentarios";
import { getCookie } from "./UsuarioService";
import { ObtenerDatosUsuario } from "./UsuarioService";

export async function ObtenerComentarios(id: number) {
    let ComentariosPost: Comentarios[] = [];
    try {
        const response = await fetch(`http://localhost:3000/api/Comentarios/ObtenerComentariosPorPublicacion/${id}`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
            },
        });
        const data: Comentarios[] = await response.json();

        const comentariosConDatos = await Promise.all(data.map(async (element) => {
            const usuario = await ObtenerDatosUsuario((element as any)["usuario_id"]);
            return {
                ...element,
                fecha: new Date(element.fecha),
                usuario: {
                    ...usuario[0],
                    nombreUsuario: usuario[0]['nombre_usuario']
                }
            };
        }));

        ComentariosPost = comentariosConDatos;

        return ComentariosPost;
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