import { Solicitud } from "../../modules/MirarSolicitud"
import { AceptarSolicitud, RechazarSolicitud, Solicitudes } from "../../services/AmigosService"
import { BuscarPorNombreUsuario } from "../../services/UsuarioService"

const agregarAmigos = document.getElementById('agregarAmigos')
const MirarSolicitudes = document.getElementById('MirarSolicitudes')
let aceptar = document.querySelectorAll('#aceptar')
let rechazar = document.querySelectorAll('#rechazar')

let band = false

MirarSolicitudes.addEventListener('click',async () => {
    if (!band) {
        agregarAmigos.innerHTML = ""
        agregarAmigos.style.display = 'flex'
        const usuarios = await Solicitudes()
        console.log(usuarios)
        MostrarSolicitudes(usuarios)
    } else {
        agregarAmigos.style.display = 'none'
    }
    band = !band;
})

function MostrarSolicitudes(usuarios){
    let datos = ''
    usuarios.forEach(element => {
        datos += Solicitud(element['amigo']['nombre_usuario'],element['amigo']['foto_perfil'],element['amigo']['id'])
    });
    agregarAmigos.innerHTML = datos
    aceptar = document.querySelectorAll('#aceptar')
    rechazar = document.querySelectorAll('#rechazar')

    aceptar.forEach((element,i) => {
        element.addEventListener('click',async () => {
            EnviarRespuesta(true,element)
            rechazar[i].style.display = 'none'
            element.innerHTML = "La solicitud fue aceptada 👌"
        })
    })

    rechazar.forEach((element,i) => {
        element.addEventListener('click', () => {
            EnviarRespuesta(false,element)
            aceptar[i].style.display = 'none'
            element.innerHTML = "La solicitud fue rechazada 🚫"
        })
    })
}

async function EnviarRespuesta(band,element){
    const nombreUsuario = element.parentElement.parentElement.childNodes[1].childNodes[3].textContent
    const usuario = await BuscarPorNombreUsuario(nombreUsuario)
    let result
    if(band){
        result = await AceptarSolicitud(usuario)
    }else{
        result = await RechazarSolicitud(usuario)
    }
}