import { getCookie } from "../../services/GetCookie"

const BotonDinamico = document.getElementById('BotonDinamico')
const Opciones = document.getElementById("options")
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
    BotonDinamico.style.display = "none"
    Opciones.style.display = "block"
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
    }
})