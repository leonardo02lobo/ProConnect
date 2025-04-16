import { FiltrarUsuario } from "./FiltrarUsuario";

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
            const datosUsuario = await BuscarUsuarioSolicitud(element['usuario2_id']);
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

async function BuscarUsuarioSolicitud(id) {
    const usuario = await FiltrarUsuario(id);
    return usuario;
}