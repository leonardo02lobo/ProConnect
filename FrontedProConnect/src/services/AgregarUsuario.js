import { getCookie } from "./GetCookie";

export async function AddUser(idUserProfile) {
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
        console.log(error.message)
    }
}

export async function RevisarSolictud(id) {
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
        console.log(error.message)
    }
}

export async function ELiminarUsuario(id) {
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
        console.log(error.message)
    }
}