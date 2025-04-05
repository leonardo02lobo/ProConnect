const BotonDinamico = document.getElementById('BotonDinamico')
const Opciones = document.getElementById("options")

addEventListener('load', async () => {
    BotonDinamico.style.display = 'none'
    Opciones.style.display = 'none'
    try {
        const response = await fetch("http://localhost:3000/api/usuario/getCookie", {
            method: "GET",
            credentials: "include",
        });
        if (!response.ok) {
            Opciones.style.display = "none"
            BotonDinamico.style.display = "block"
            return;
        }
        BotonDinamico.style.display = "none"
        Opciones.style.display = "block"
    } catch (e) {
        console.log("Error al verificar autenticación:");
    }
})

Opciones.addEventListener('click', () => {
    if (Opciones.value === "cerrarSesion") {
        window.location.href = "/CerrarSesion"
    } else if (Opciones.value === "mirarPerfilUsuario") {
        window.location.href = "/MirarPerfilUsuario"
    }
})