const formulario = document.getElementById('formulario') as HTMLFormElement;
const recuperar = document.getElementById('recuperar') as HTMLElement | null;
import type { Usuario } from "../../models/Usuario"
const usuario: Usuario = {
    nombre: "",
    nombreUsuario: "",
    email: "",
    contrasena: "",
    fotoPerfil: "",
    fotoFondo: "",
    puesto: "",
    tipoUsuario: ""
}

recuperar?.addEventListener('click', (e) => {
    e.preventDefault()
    console.log('Enviando...');
    CambiarContrasenia()
})

async function CambiarContrasenia() {
    const isTrue: Boolean = await ObtenerDatos();
    if (isTrue) {
        try {
            const response = await fetch('http://localhost:3000/api/usuario/SetPassword', {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(usuario)
            });
            if (response.ok) {
                window.location.href = "/IniciarSesion";
            }
        } catch (e) {
            if (e instanceof Error) {
                console.log(e.message);
            } else {
                console.log('An unknown error occurred');
            }
            alert('Error al cambiar la contraseña')
        }
    }
}

async function ObtenerDatos(): Promise<Boolean> {
    const promise: Boolean = await ValidarDatos();
    if (promise) {
        usuario.nombreUsuario = formulario?.nombreUsuario?.value;
    } else {
        return false;
    }
    if (formulario?.contraseña?.value === formulario?.Confirmarcontraseña?.value) {
        usuario.contrasena = formulario?.contraseña?.value
    } else {
        return false;
    }
    return true;
}

async function ValidarDatos(): Promise<Boolean> {
    try {
        usuario.nombreUsuario = formulario?.nombreUsuario.value;
        const response = await fetch('http://localhost:3000/api/usuario/FindUser', {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(usuario)
        });
        const result = await response.json()
        if (result.length !== 0) {
            return true;
        } else {
            alert("El nombre de Usuario no existe")
            return false;
        }
    } catch (e) {
        if (e instanceof Error) {
            console.log(e.message);
        } else {
            console.log('An unknown error occurred');
        }
        alert('El nombre de Usuario no existe')
    }
    return false;
}