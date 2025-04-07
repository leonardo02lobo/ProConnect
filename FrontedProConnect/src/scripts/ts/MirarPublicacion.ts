import type { Comentarios } from "../../models/Comentarios";
import type { Usuario } from "../../models/Usuario";

export async function ObtenerNumerosLikes(id: number) {
    const likes = await fetch(
        `http://localhost:3000/api/Publicacion/Publicacion/BuscarLikes/${id}`,
        {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
            },
        },
    );
    const result: string[] = (await likes.json()) as string[];
    return result;
}

export async function ObtenerPublicacion(id: number | undefined) {
    if (!id) {
        throw new Error("ID is undefined");
    }
    const response = await fetch(
        `http://localhost:3000/api/Publicacion/Publicacion/${id}`,
        {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
            },
        },
    );
    if (!response.ok) {
        throw new Error(`Error: ${response.status}`);
    }
    const data = await response.json();
    const publicaciones = {
        ...data,
        contenido: data[0]["contenido"],
        foto: data[0]["foto"],
        titulo: data[0]["titulo"],
        id: data[0]["id"],
        fecha: data[0]["fecha"],
    };
    const usuarioData = await ObtenerDatosUsuario(data[0]["usuario_id"])
    publicaciones.usuario = {
        ...usuarioData[0],
        nombreUsuario: usuarioData[0]["nombre_usuario"],
    };
    return publicaciones;
}

async function ObtenerDatosUsuario(usuarioId: number) {
    const response2 = await fetch(
        `http://localhost:3000/api/usuario/Filtrar/${usuarioId}`,
        {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
            },
        },
    );

    return await response2.json();}

export async function ObtenerComentarios(id: number){
    let ComentariosPost: Comentarios[] = []
    const response = await fetch(`http://localhost:3000/api/Comentarios/ObtenerComentariosPorPublicacion/${id}`,{
        method: "GET",
        headers: {
            "Content-Type": "application/json",
        },
    })
    const data: Comentarios[] = await response.json();
    const usuario = await ObtenerDatosUsuario((data as any)[0]["usuario_id"])
    data.map(async (element: Comentarios) => {
        element = {
            ...element,
            fecha: new Date(element.fecha),
            usuario : {
                ...usuario[0],
                nombreUsuario: usuario[0]['nombre_usuario']
            }
        }
        ComentariosPost.push(element)
    })
    return ComentariosPost;
}