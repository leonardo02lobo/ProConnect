import type { Usuario } from "../models/Usuario";
import type { PublicacionModel } from "./../models/Publicacion";
import { getImageUrl } from "./ImagesService";
import { ObtenerDatosUsuario } from "./UsuarioService";

export async function setPublicacion(publicacion: PublicacionModel) {
    try {
        const response = await fetch('http://localhost:3000/api/Publicacion/CrearPublicacion', {
            method: "POST",
            credentials: "include",
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(publicacion)
        })
        return await response.json();
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
        for (const element of data) {
            if (element.usuario.fotoPerfil !== "") {
                const result = await getImageUrl(element.usuario.fotoPerfil);
                element.usuario.fotoPerfil = result;
            }
            if (element.foto !== "") {
                const result = await getImageUrl(element.foto);
                element.foto = result;
            }
        }
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
    if (data.usuario.fotoPerfil !== "") {
        const result = await fetch(`http://localhost:3000${data.usuario.fotoPerfil}`);
        data.usuario.fotoPerfil = result.url;
    }
    if (data.foto !== "") {
        const result = await getImageUrl(data.foto);
        data.foto = result;
    }
    return data;
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

export async function ObtenerPublicacionesUsuario(usuario: Usuario): Promise<PublicacionModel[]> {
    try {
        const response = await fetch("http://localhost:3000/api/Publicacion/TodasLasPublicaciones", {
            method: "POST",
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(usuario)
        });
        const data: PublicacionModel[] = await response.json();
        for (const element of data) {
            if (element.usuario.fotoPerfil !== "") {
                const result = await getImageUrl(element.usuario.fotoPerfil);
                element.usuario.fotoPerfil = result;
            }
            if (element.foto !== "") {
                const result = await getImageUrl(element.foto);
                element.foto = result;
            }
        }
        return data;
    } catch (e) {
        console.log((e as Error).message)
        console.log("Error al intentar obtener la publicacion")
        return [];
    }
}

export async function SubirImagenFotoPublicacion(input: HTMLInputElement | null,id: number) {
    try {
        const formData = new FormData();

        if (input && input.files && input.files[0]) {
            formData.append('image', input.files[0]); 
        }
        formData.append('id', id.toString());

        const response = await fetch(`http://localhost:3000/api/Publicacion/ActualizarFoto`,{
            method: "POST",
            credentials: "include",
            body: formData,
        });

        if (!response.ok) {
            throw new Error(`Error ${response.status}: ${response.statusText}`);
        }

        return await response.json();
    } catch (error) {
        console.error('Error al subir imagen:', error);
        alert((error as Error).message);
        return null;
    }
}