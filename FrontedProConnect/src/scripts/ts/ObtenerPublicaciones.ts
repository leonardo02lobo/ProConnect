import type { PublicacionModel } from "../../models/Publicacion";

export async function ObtenerPublicacionesIndex() : Promise<PublicacionModel[]>{
    try {
        const response = await fetch("http://localhost:3000/api/Publicacion", {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
            },
        });
        const data = await response.json();
        const publicaciones: PublicacionModel[] = data;
        for (let i = 0; i < publicaciones.length; i++) {
            const usuarioId = data[i]["usuario_id"];
            const response2 = await fetch(
                `http://localhost:3000/api/usuario/Filtrar/${usuarioId}`,
                {
                    method: "GET",
                    headers: {
                        "Content-Type": "application/json",
                    },
                },
            );
        
            const usuarioData = await response2.json();
            publicaciones[i].usuario = {
                ...usuarioData[0],
                nombreUsuario: usuarioData[0]['nombre_usuario']
            }
        }
        return publicaciones;
    } catch (e) {
        return [];
    }
}