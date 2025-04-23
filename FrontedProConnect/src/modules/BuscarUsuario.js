import { getImageUrl } from "../services/ImagesService"

export async function TarjetaUsuario(fotoPerfil, nombre, id) {
    const result = await getImageUrl(fotoPerfil)
    return `
        <div class="w-full dark:bg-gray-800 rounded-3xl p-4 flex flex-row gap-5 items-center border-2 border-gray-500">
            <img src=${result == "" ? "/assets/ImagenesPerfil/predeterminado.png" : result} alt="" class="w-20 rounded-3xl">
            <a href="/MirarUsuarios/${id}">${nombre}</a>
        </div>
    `
}