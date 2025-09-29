import { getCookie } from "./../../services/UsuarioService"

const BotonDinamico = document.getElementById('BotonDinamico')
const Opciones = document.getElementById("options")
const amigos = document.getElementById('amigos')
const empleo = document.getElementById('empleo')
const index = document.getElementById('index')
const mensajes = document.getElementById('mensajes')

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

    empleo.style.display = 'flex'
    if(response.tipoUsuario === "usuario"){
        VisibilidadBotones(empleo.children[0],"Buscar Empleo",3)
    }else if(response.tipoUsuario === "empresa"){
        VisibilidadBotones(empleo.children[1],"Mirar Postulacion",3)
    }
    BotonDinamico.style.display = "none"
    Opciones.style.display = "block"
    amigos.style.display = "block"
    VisibilidadBotones(mensajes,"Mensajes",3)
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
    }else if(Opciones.value === "Mensajes"){
        window.location.href = "/Mensajes"
    }else if(Opciones.value === "Buscar Empleo"){
        window.location.href = "/BuscarEmpleo"
    }else if(Opciones.value === "Mirar Postulacion"){
        window.location.href = "/MirarPostulaciones"
    }
})

function VisibilidadBotones(component,text,n){
    if(window.screen.width > 1000){
        component.style.display = "block"
    }else{
        const element = document.createElement("option")
        element.innerHTML = text
        element.value = text.trim()
        Opciones.insertBefore(element,Opciones.options[n])
    }
}