import { publicacionesPerfil } from "../../modules/PublicacionesModules"
import { getCookie } from "../../services/UsuarioService"
import { ObtenerPublicacionesUsuario } from "../../services/PublicacionesService"
import { ObtenerSeguidores } from "../../services/AmigosService"
import { getImageUrl } from "../../services/ImagesService"

const imagenPerfil = document.querySelectorAll('#FotoPerfil')
const nombre = document.getElementById('nombre')
const puesto = document.getElementById('puesto')
const publicaciones = document.getElementById('publicaciones')
const NSeguidores = document.getElementById('NSeguidores')
const publicarEmpleo = document.getElementById('publicarEmpleo')

addEventListener('load', async () => {
    const data = await getCookie();

    if(data.tipoUsuario === "empresa"){
        publicarEmpleo.style.display = 'block'
        publicarEmpleo.children[0].href = "/PublicarEmpleo"
    }

    window.document.title = `Perfil-${data.nombreUsuario}`
    const dataSeguidores = await ObtenerSeguidores(data.id)
    try {
        if (!data) {
            window.location.href = "/"
            return;
        }

        if(data.fotoPerfil !== ""){
            ObtenerImagen(data)
        }
        NSeguidores.innerHTML = dataSeguidores + " seguidores"
        NSeguidores.href = `/MirarAmigos/Seguidores/${data.id}`
        nombre.innerHTML = data.nombreUsuario
        puesto.innerHTML = data.puesto
        await MostrarPublicaciones(data)
    } catch (error) {
        console.log(error.message)
    }
})

async function MostrarPublicaciones(usuario) {
    const data = await ObtenerPublicacionesUsuario(usuario);
    let htmlPromises = data.map(async (element) => {
        return await publicacionesPerfil(element.foto, element.usuario.fotoPerfil, element.titulo, element.usuario.nombreUsuario, element.contenido);
    });

    let htmlArray = await Promise.all(htmlPromises);
    publicaciones.innerHTML = htmlArray.join('');
}

async function ObtenerImagen(data){
    const result = await getImageUrl(data.fotoPerfil)
    data.fotoPerfil = result
    if (result !== '') {
        imagenPerfil[0].src = result
        if (imagenPerfil[1])
            imagenPerfil[1].src = result
    } else {
        imagenPerfil[0].src = "/assets/ImagenesPerfil/predeterminado.png"
        if (imagenPerfil[1])
            imagenPerfil[1].src = "/assets/ImagenesPerfil/predeterminado.png"
    }
}