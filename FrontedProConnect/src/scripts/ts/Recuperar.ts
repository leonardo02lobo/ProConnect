const formulario = document.getElementById('formulario') as HTMLFormElement;
const recuperar = document.getElementById('recuperar') as HTMLElement | null;
import type { Usuario } from "../../models/Usuario"
import { EncontrarUsuario, NuevaContrasenia } from "../../services/UsuarioService";
const usuario: Usuario = {
    nombre: "",
    nombreUsuario: "",
    email: "",
    contrasena: "",
    fotoPerfil: "",
    fotoFondo: "",
    puesto: "",
    tipoUsuario: "",
    id: 0
}

recuperar?.addEventListener('click', (e) => {
    e.preventDefault()
    CambiarContrasenia()
})

async function CambiarContrasenia() {
    const isTrue: Boolean = await ObtenerDatos();
    if (isTrue) {
        try {
            const response = await NuevaContrasenia(usuario)
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
        const result = await EncontrarUsuario(usuario)
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