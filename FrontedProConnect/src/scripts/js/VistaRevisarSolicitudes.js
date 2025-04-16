import { Solicitud } from "../../modules/MirarSolicitud"
import { Solicitudes } from "../../services/BuscarSolicitudes"

const agregarAmigos = document.getElementById('agregarAmigos')
const MirarSolicitudes = document.getElementById('MirarSolicitudes')

let band = false

MirarSolicitudes.addEventListener('click',async () => {
    if (!band) {
        agregarAmigos.innerHTML = ""
        agregarAmigos.style.display = 'flex'
        const usuarios = await Solicitudes()
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
}
