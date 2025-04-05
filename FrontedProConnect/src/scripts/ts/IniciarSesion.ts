import type { Usuario } from "../../models/Usuario";
let usuario: Usuario = {
    nombre: "",
    nombreUsuario: "",
    email: "",
    contrasena: "",
    fotoPerfil: "",
    fotoFondo: "",
    puesto: "",
    tipoUsuario: ""
}
const formulario = document.getElementById("formulario") as HTMLFormElement | null;

export function LoginUser() {
    usuario.nombre = formulario?.usuario?.value;
    usuario.contrasena = formulario?.contraseña?.value;
    return usuario;
}