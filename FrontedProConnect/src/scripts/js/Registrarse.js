import { setEmpresa } from "../../services/EmpresasService"
import { SetUsuario } from "../../services/UsuarioService.ts"
import { GuardarDatosUsuario, GuardarDatosExtraUsuario, GuardarDatosEmpresa, ObtenerUsuario} from "./../ts/Registrarse.ts"
const datos = document.getElementById("datos")
const datosExtra = document.getElementById("datosExtra")
const iniciarSesion = document.getElementById("iniciarSesion")
const iniciarSesionBoton = document.getElementById("iniciarSesionBoton")
const Crear = document.getElementById("crear")
const Terminar = document.getElementById("terminar")
const tipoUsuario = document.getElementById("tipoUsuario")
const ExtraEmpresa = document.getElementById("ExtraEmpresa")
const terminar2 = document.getElementById('terminar2')

let band = false;

if (datosExtra && iniciarSesion) {
    datosExtra.style.display = "none";
    iniciarSesion.style.display = "none";
    ExtraEmpresa.style.display = "none";
}

Crear?.addEventListener('click', async (e) => {
    e.preventDefault()
    if (await GuardarDatosUsuario()) {
        datos.classList.add("opacity-0")
        datos.classList.add("translate-y-1/2")
        datos.classList.add("duration-700")
    }
})

datos?.addEventListener('transitionend', () => {
    if (datosExtra && datos) {
        datos.style.display = "none";
        datosExtra.style.display = "block";
    }
}, { once: true })

tipoUsuario.addEventListener('click', () => {
    if (tipoUsuario.value === "usuario") {
        Terminar.innerHTML = "Terminar"
        band = true;
    } else {
        Terminar.innerHTML = "Siguiente"
        band = false;
    }
})

Terminar?.addEventListener('click', (e) => {
    e.preventDefault()
    if (GuardarDatosExtraUsuario()) {
        datosExtra.classList.add("opacity-0")
        datosExtra.classList.add("translate-y-1/2")
        datosExtra.classList.add("duration-700")
    }
})

datosExtra?.addEventListener('transitionend', (e) => {
    if (datosExtra && iniciarSesion && band) {
        datosExtra.style.display = "none";
        iniciarSesion.style.display = "block";
    } else {
        ExtraEmpresa.style.display = "block"
        datosExtra.style.display = "none";
    }
}, { once: true })

terminar2?.addEventListener('click', (e) => {
    e.preventDefault()
    if (GuardarDatosEmpresa()) {
        ExtraEmpresa.classList.add("opacity-0")
        ExtraEmpresa.classList.add("translate-y-1/2")
        ExtraEmpresa.classList.add("duration-700")
    }
})

ExtraEmpresa?.addEventListener('transitionend', () => {
    if (ExtraEmpresa && iniciarSesion) {
        ExtraEmpresa.style.display = "none";
        iniciarSesion.style.display = "block";
    }
})

iniciarSesionBoton?.addEventListener('click', async (e) => {
    e.preventDefault()
    if (band) {
        RegistrarUsuario();
    } else {
        RegistrarEmpresa();
    }
})

async function RegistrarUsuario() {
    const result = await SetUsuario(ObtenerUsuario())
    if (result.ok) {
        alert(`Usuario creado: ${result.message}`);
        window.location.href = "/IniciarSesion";
    } else {
        throw new Error(result.error || 'Error desconocido');
    }
}

async function RegistrarEmpresa() {
    const result = await setEmpresa();
    if (result.ok) {
        alert(`Empresa creada: ${result.message}`);
        window.location.href = "/IniciarSesion";
    } else {
        throw new Error(result.error || 'Error desconocido');
    }
}