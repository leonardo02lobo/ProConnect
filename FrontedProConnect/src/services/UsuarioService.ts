import type { Usuario } from "../models/Usuario";

export async function ObtenerDatosUsuario(usuarioId: number) {
    const response2 = await fetch(
        `http://localhost:3000/api/usuario/Filtrar/${usuarioId}`,
        {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
            },
        },
    );

    return await response2.json();
}

export async function SetUsuario(usuario: Usuario) {
    try {
        const response = await fetch('http://localhost:3000/api/usuario/crearUsuario', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(usuario)
        })
        return response;
    } catch (error) {
        alert((error as Error).message);
    }
}

export async function NuevaContrasenia(usuario: Usuario){
    const response = await fetch('http://localhost:3000/api/usuario/SetPassword', {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(usuario)
    });
    return response;
}

export async function EncontrarUsuario(usuario: Usuario){
    const response = await fetch('http://localhost:3000/api/usuario/FindUser', {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(usuario)
    });
    return await response.json();
}

export async function ValidarCorreoElectronico(usuario: Usuario): Promise<boolean> {
    try {
        const response = await fetch('http://localhost:3000/api/usuario/ValidarEmail', {
            method: "POST",
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(usuario)
        });
        const result: boolean = await response.json();
        return result
    } catch (e) {
        console.log((e as Error).message);
        return false;
    }
}

export async function ValidarNombreUsuario(usuario: Usuario): Promise<boolean> {
    try {
        const response = await fetch('http://localhost:3000/api/usuario/ValidarNombreUsuario', {
            method: "POST",
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(usuario)
        });
        const result: boolean = await response.json();
        return result
    } catch (e) {
        console.log((e as Error).message);
        return false;
    }
}

export async function BuscarPersona(name: string){
    const response = await fetch(`http://localhost:3000/api/usuario/Buscador/${name}`,{
        method: "GET",
        headers:{
            "Content-Type": "application/json"
        },
    })
    const data = await response.json()
    console.log(data)
    return data
}

export async function FiltrarUsuario(id: number) {
    try {
        const response = await fetch(`http://localhost:3000/api/usuario/Filtrar/${id}`)

        const data = await response.json()
        return data[0]
    } catch (e) {
        return []
    }
}

export async function BuscarPorNombreUsuario(name: string) {
    try {
        const response = await fetch(`http://localhost:3000/api/usuario/FiltrarNombre/${name.trim()}`)
        const data = await response.json()
        return data[0]
    } catch (e) {
        return []
    }
}

export async function getCookie() {
    try {
        const response = await fetch("http://localhost:3000/api/usuario/getCookie", {
            method: "GET",
            credentials: "include",
        });
        if (!response.ok) {
            console.log(response)
            return null;
        } 
        const data = await response.json();
        if (data) {
            return data['user']
        } else {
            return null;
        }
    } catch (e) {
        console.log("Error al verificar autenticación:");
        return null;
    }
}

export async function IniciarSesion(usuario: Usuario) {
    try {
        const response = await fetch('http://localhost:3000/api/usuario/loginUser', {
            method: 'POST',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(usuario)
        })
        return response
    } catch (error) {
        alert((error as Error).message);
    }
}