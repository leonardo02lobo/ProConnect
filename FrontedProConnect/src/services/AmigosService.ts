import type { Amigos } from "../models/Amigos";
import type { Usuario } from "../models/Usuario"
import { getCookie } from "./UsuarioService"
import { FiltrarUsuario } from "./UsuarioService";

export async function AddUser(idUserProfile: number) {
    try {
        const userData = await getCookie()
        const response = await fetch('http://localhost:3000/api/Amigos/AgregarAmigos', {
            method: "POST",
            credentials: "include",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                usuario1ID: userData.id,
                usuario2ID: idUserProfile
            })
        })
        if (response.ok) {
            window.location.reload()
        }
    } catch (error) {
        console.log((error as Error).message)
    }
}

export async function RevisarSolictud(id: number) {
    try {
        const response = await fetch(`http://localhost:3000/api/Amigos/BuscarTodosLosAmigos/${id}`, {
            method: "GET",
            credentials: "include",
            headers: {
                "Content-Type": "application/json"
            }
        })
        const data = await response.json();
        if (!response.ok) {
            console.log("Error")
            return null;
        }
        if (data.estado === "Pendiente") {
            return "🕧 Pendiente"
        } else if (data.estado === "Cancelado") {
            return "Conectar +"
        }
    } catch (error) {
        console.log((error as Error).message)
    }
}

export async function ELiminarUsuario(id: number) {
    try {
        const response = await fetch(`http://localhost:3000/api/Amigos/EliminarSolicitud/${id}`, {
            method: "GET",
            credentials: "include",
            headers: {
                "Content-Type": "application/json"
            }
        })
        const data = await response.json();
        if (!response.ok) {
            console.log("Error")
            return null;
        }

        if (data !== null) {
            window.location.reload()
        }
    } catch (error) {
        console.log((error as Error).message)
    }
}

export async function Solicitudes() {
    let usuarios = [];
    try {
        const response = await fetch('http://localhost:3000/api/Amigos/BuscarSolicitudes', {
            method: "GET",
            credentials: "include",
            headers: {
                "Content-Type": "application/json"
            }
        });
        const data = await response.json();

        for (const element of data) {
            const datosUsuario = await BuscarUsuarioSolicitud(element['usuario1_id']);
            const elementos = {
                id: element['id'],
                estado: element['estado'],
                idUsuario: element['usuario1_id'],
                amigo: datosUsuario,
            };
            usuarios.push(elementos);
        }
        return usuarios;
    } catch (e) {
        console.error(e);
        return [];
    }
}

export async function AceptarSolicitud(usuario: Usuario) {
    const usuarioCookie = await getCookie()
    try {
        const response = await fetch('http://localhost:3000/api/Amigos/AceptarSolicitud', {
            method: "POST",
            credentials: "include",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                usuario1ID: usuario.id,
                usuario2ID: usuarioCookie.id,
            })
        })
        return response;
    } catch (error) {
        console.log((error as Error).message)
        return null;
    }
}

export async function RechazarSolicitud(usuario: Usuario) {
    const usuarioCookie = await getCookie()
    try {
        const response = await fetch('http://localhost:3000/api/Amigos/RechazarSolicitud', {
            method: "POST",
            credentials: "include",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                usuario1ID: usuario.id,
                usuario2ID: usuarioCookie.id,
            })
        })
        return response;
    } catch (error) {
        console.log((error as Error).message)
        return null;
    }
}

export async function ObtenerSeguidores(id: number) {
    try {
        const response = await fetch(`http://localhost:3000/api/Amigos/BuscarNumeroSeguidores/${id}`, {
            method: "GET",
            credentials: "include",
        });
        const data = await response.json();
        return data[0]["COUNT(*)"]
    } catch (e) {
        console.log("Error al verificar autenticación:");
    }
}

export async function ObtenerDatosSeguidores(id: number) : Promise<Amigos[]>{
    try {
        const response = await fetch(`http://localhost:3000/api/Amigos/BuscarSeguidores/${id}`);
        if (!response.ok) {
            console.log("Se rechazo la solicitud")
            return []
        }
        const data: Amigos[] = await response.json();
        return data;
    } catch (error) {
        console.log((error as Error).message)
        return []
    }
}

async function BuscarUsuarioSolicitud(id: number) {
    const usuario = await FiltrarUsuario(id);
    return usuario;
}