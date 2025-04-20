import { getCookie } from "./../../services/UsuarioService"

const BotonDinamico = document.getElementById('BotonDinamico')
const Opciones = document.getElementById("options")
const amigos = document.getElementById('amigos')
const empleo = document.getElementById('empleo')
const index = document.getElementById('index')

index.addEventListener('click', () => {
    window.location.href = "/"
})

addEventListener('load', async () => {
    BotonDinamico.style.display = 'none'
    Opciones.style.display = 'none'
    const response = await getCookie();
    if (response == null) {
        Opciones.style.display = "none"
        BotonDinamico.style.display = "block"
        return;
    }

    if(response.tipoUsuario === "usuario"){
        empleo.style.display = 'flex'
    }
    BotonDinamico.style.display = "none"
    Opciones.style.display = "block"
    amigos.style.display = "block"
});

BotonDinamico.addEventListener('click', () => {
    window.location.href = "/"
    window.location.href = "/IniciarSesion"
});

Opciones.addEventListener('click', () => {
    if (Opciones.value === "cerrarSesion") {
        window.location.href = "/"
        window.location.href = "/CerrarSesion"
    } else if (Opciones.value === "mirarPerfilUsuario") {
        window.location.href = "/MirarPerfilUsuario"
    }else if(Opciones.value === "opciones"){
        window.location.href = "/Opciones"
    }
})