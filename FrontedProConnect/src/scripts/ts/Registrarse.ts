import type { Usuario } from "../../models/Usuario";
import type { Empresas } from "../../models/Empresas";
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
const empresas: Empresas = {
    nombreEmpresa: "",
    descripcion: "",
    pais: "",
    logo: "",
    usuarioEmpresa: usuario,
}
const formulario = document.getElementById('formulario') as HTMLFormElement | null;
const warningElement = document.querySelectorAll("#warning") as NodeListOf<HTMLElement> | null;
const inputsElement = document.querySelectorAll('.input') as NodeListOf<HTMLInputElement> | null;

export async function GuardarDatosUsuario() {
    if (formulario) {
        usuario.nombre = formulario.nombre?.value as string;
        usuario.nombreUsuario = formulario.nombreUsuario?.value as string;
        usuario.email = formulario.email?.value as string;
        usuario.contrasena = formulario.contraseña?.value as string;
        if(!usuario.email.includes('@') || !usuario.email.includes('.com')){
            alert("El correo que proporcionaste es invalido")
            return false;
        }
        if (formulario.nombre?.value !== "" && formulario.nombreUsuario?.value !== ""
            && formulario.email?.value !== "" && formulario.contraseña?.value !== ""
            && formulario.confirmarContraseña?.value !== "") {
            if (formulario?.contraseña?.value === formulario?.confirmarContraseña?.value) {
                if (inputsElement) {
                    if (inputsElement[5].checked) {
                        if (await ValidarNombreUsuario()) {
                            alert('El nombre de usuario Agregado ya esta en uso.Por favor Ingresa otro')
                            return false;
                        }
                        if (await ValidarCorreoElectronico()) {
                            alert('El correo Agregado ya esta en uso.Por favor Ingresa otro')
                            return false;
                        }
                        IniciarSesion();
                        return true;
                    } else {
                        alert('Debes aceptar los terminos y condiciones');
                        return false;
                    }
                }

            } else {
                alert('Las contraseñas no coinciden');
                return false;
            }
        } else {
            mostrarWarning("block");
            return false;
        }
    }
}

export function GuardarDatosExtraUsuario() {
    usuario.puesto = formulario?.puesto?.value;
    usuario.tipoUsuario = formulario?.tipoUsuario?.value;
    if (usuario.puesto !== "" && usuario.tipoUsuario !== "") {
        return true;
    } else {
        alert('Debes llenar todos los campos');
        return false;
    }
}

export function GuardarDatosEmpresa() {
    empresas.nombreEmpresa = formulario?.NombreEmpresa?.value;
    empresas.descripcion = formulario?.Descripcion?.value;
    empresas.pais = formulario?.pais?.value;
    empresas.usuarioEmpresa = usuario;
    if (empresas.nombreEmpresa !== "" && empresas.pais !== "") {
        return true;
    } else {
        alert('Debes llenar todos los campos');
        return false;
    }
}

export function ObtenerUsuario(): Usuario {
    return usuario;
}

export function ObtenerEmpresa(): Empresas {
    return empresas;
}

function mostrarWarning(tipo: string = "none"): void {
    warningElement?.forEach((element, i) => {
        if (inputsElement) {
            if (inputsElement[i].value === "") {
                element.style.display = tipo;
            }
        }
    })
}

function IniciarSesion(): boolean {
    mostrarWarning();
    return true;
}

async function ValidarCorreoElectronico(): Promise<boolean> {
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

async function ValidarNombreUsuario(): Promise<boolean> {
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