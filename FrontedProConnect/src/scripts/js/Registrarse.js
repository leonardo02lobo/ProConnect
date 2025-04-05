import { GuardarDatosUsuario, GuardarDatosExtraUsuario, ObtenerUsuario, GuardarDatosEmpresa, ObtenerEmpresa } from "./../ts/Registrarse.ts"
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

Crear?.addEventListener('click',async (e) => {
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
    try {
        const response = await fetch('http://localhost:3000/api/usuario/crearUsuario', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(ObtenerUsuario())
        })
        const result = await response.json();
        if (response.ok) {
            alert(`Usuario creado: ${result.message}`);
            window.location.href = "/IniciarSesion";
        } else {
            throw new Error(result.error || 'Error desconocido');
        }
    } catch (error) {
        console.error('Error:', error);
        alert(error.message);
    }
}

async function RegistrarEmpresa() {
    try {
        const response = await fetch('http://localhost:3000/api/Empresas/CrearEmpresa', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(ObtenerEmpresa())
        })
        const result = await response.json();
        console.log(result)
        if (response.ok) {
            alert(`Empresa creada: ${result.message}`);
            window.location.href = "/IniciarSesion";
        } else {
            throw new Error(result.error || 'Error desconocido');
        }
    } catch (error) {
        console.error('Error:', error);
        alert(error.message);
    }
}