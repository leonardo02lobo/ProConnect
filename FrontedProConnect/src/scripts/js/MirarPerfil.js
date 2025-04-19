import { publicacionesPerfil } from "../../modules/PublicacionesModules"
import { getCookie } from "../../services/UsuarioService"
import { ObtenerPublicacionesUsuario } from "../../services/PublicacionesService"
import { ObtenerSeguidores } from "../../services/AmigosService"

const imagenPerfil = document.querySelectorAll('#FotoPerfil')
const nombre = document.getElementById('nombre')
const puesto = document.getElementById('puesto')
const publicaciones = document.getElementById('publicaciones')
const NSeguidores = document.getElementById('NSeguidores')

addEventListener('load', async () => {
    const data = await getCookie();
    window.document.title = `Perfil-${data.nombreUsuario}`
    const dataSeguidores = await ObtenerSeguidores(data.id)
    try {
        if (!data) {
            window.location.href = "/"
            return;
        }
        NSeguidores.innerHTML = dataSeguidores + " seguidores"
        NSeguidores.href = `/MirarAmigos/Seguidores/${data.id}`
        if (data.fotoPerfil !== '') {
            imagenPerfil[0].src = data.fotoPerfil
            if (imagenPerfil[1])
                imagenPerfil[1].src = data.fotoPerfil
        } else {
            imagenPerfil[0].src = "/assets/ImagenesPerfil/predeterminado.png"
            if (imagenPerfil[1])
                imagenPerfil[1].src = "/assets/ImagenesPerfil/predeterminado.png"
        }
        nombre.innerHTML = data.nombreUsuario
        puesto.innerHTML = data.puesto
        await MostrarPublicaciones(data)
    } catch (error) {
        console.log(error.message)
    }
})

async function MostrarPublicaciones(usuario) {
    const data = await ObtenerPublicacionesUsuario(usuario);
    data.forEach(element => {
        publicaciones.innerHTML += publicacionesPerfil(element['foto'], element['foto_perfil'], element['titulo'], element['nombre_usuario'], element['contenido'])
    });
}