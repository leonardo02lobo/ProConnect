import type { Usuario } from "../models/Usuario";
import type { PublicacionModel } from "./../models/Publicacion";
import { ObtenerDatosUsuario } from "./UsuarioService";

export async function setPublicacion(publicacion: PublicacionModel){
    try {
        const response = await fetch('http://localhost:3000/api/Publicacion/CrearPublicacion', {
            method: "POST",
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(publicacion)
        })
        return response;
    } catch (e) {
        console.log("Error al verificar autenticacion");
    }
}

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

export async function ObtenerPublicacionesIndex(): Promise<PublicacionModel[]> {
    try {
        const response = await fetch("http://localhost:3000/api/Publicacion", {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
            },
        });
        const data = await response.json();
        return data;
    } catch (e) {
        return [];
    }
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
export async function DarLikeId(id: number) {
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
export async function QuitarLikeId(id: number) {
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

export async function ObtenerPublicacionesUsuario(usuario: Usuario): Promise<PublicacionModel[]>{
    try{
        const response = await fetch("http://localhost:3000/api/Publicacion/TodasLasPublicaciones",{
            method: "POST",
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(usuario)
        });
        const data: PublicacionModel[] = await response.json();
        return data;
    }catch(e){
        console.log((e as Error).message)
        console.log("Error al intentar obtener la publicacion")
        return [];
    }
}