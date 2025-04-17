import { FiltrarUsuario } from "./FiltrarUsuario";
import { getCookie } from "./GetCookie";

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

export async function AceptarSolicitud(usuario){
    const usuarioCookie = await getCookie()
    try {
        const response = await fetch('http://localhost:3000/api/Amigos/AceptarSolicitud',{
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
        console.log(error.message)
        return null;
    }
}

export async function RechazarSolicitud(usuario){
    const usuarioCookie = await getCookie()
    try {
        const response = await fetch('http://localhost:3000/api/Amigos/RechazarSolicitud',{
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
        console.log(error.message)
        return null;
    }
}

async function BuscarUsuarioSolicitud(id) {
    const usuario = await FiltrarUsuario(id);
    return usuario;
}