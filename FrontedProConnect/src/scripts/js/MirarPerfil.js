import { DatosUsuario, ObtenerPublicacionesUsuario } from "../../services/Perfil"

const imagenPerfil = document.querySelectorAll('#FotoPerfil')
const nombre = document.getElementById('nombre')
const puesto = document.getElementById('puesto')
const publicaciones = document.getElementById('publicaciones')

addEventListener('load', async () => {
    const data = await DatosUsuario();
    if (!data) {
        window.location.href = "/"
        return;
    }
    if (data.user.row[0]['foto_perfil'] !== '') {
        imagenPerfil[0].src = data.user.row[0]['foto_perfil']
        imagenPerfil[1].src = data.user.row[0]['foto_perfil']
    } else {
        imagenPerfil[0].src = "/assets/ImagenesPerfil/predeterminado.png"
        imagenPerfil[1].src = "/assets/ImagenesPerfil/predeterminado.png"
    }
    nombre.innerHTML = data.user.row[0]['nombre_usuario']
    puesto.innerHTML = data.user.row[0].puesto
    await MostrarPublicaciones(data.user.row[0])
})

async function MostrarPublicaciones(usuario) {
    const data = await ObtenerPublicacionesUsuario(usuario);
    data.forEach(element => {
        publicaciones.innerHTML += `
            <a>
                <div
                    class="flex flex-col p-4 gap-3 b-2 bg dark:bg-gray-800 bg-white shadow-lg border border-gray-200 rounded-lg mb-5 cursor-pointer"
                >
                    <div class="flex flex-col gap-3">
                                <img
                                    src=${element['foto']}
                                    alt="fotoPost"
                                    class="w-[100%] rounded-3xl"
                                />
                        <div class="flex flex-row gap-4">
                            <a href="/MirarPerfil">
                                <img
                                    src=${element['foto_perfil'] || "/assets/ImagenesPerfil/predeterminado.png"}
                                    alt="fotoPerfil"
                                    class="rounded-3xl w-20 h-20"
                                />
                            </a>
                            <div class="flex flex-col gap-1">
                                <h1 class="font-bold">${element['titulo']}</h1>
                                    <span class="hover:underline"><a>${element['nombre_usuario']}</a></span>
                                <p>${element['contenido']}</p>
                            </div>
                        </div>
                    </div>
                </div>
            </a>
            `
    });
}