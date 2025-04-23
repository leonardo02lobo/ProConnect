import { getImageUrl } from "../services/ImagesService"

export async function publicacionesPerfil(foto, fotoPerfil, titulo, nombreUsuario, contenido) {
    let resultfoto = ''
    if(foto !== ""){
        resultfoto = await getImageUrl(foto)
    }
    const result = await getImageUrl(fotoPerfil)
    return `
            <a>
                <div
                    class="flex flex-col p-4 gap-3 b-2 bg dark:bg-gray-800 bg-white shadow-lg border border-gray-200 rounded-lg mb-5 cursor-pointer"
                >
                    <div class="flex flex-col gap-3">
                                ${(resultfoto !== "") ? `<img src="${resultfoto}" alt="fotoPost" class="w-[100%] rounded-3xl"/>` : ''}
                        <div class="flex flex-row gap-4">
                            <a href="/MirarPerfil">
                                <img
                                    src=${result || "/assets/ImagenesPerfil/predeterminado.png"}
                                    alt="fotoPerfil"
                                    class="rounded-3xl w-20 h-20"
                                />
                            </a>
                            <div class="flex flex-col gap-1">
                                <h1 class="font-bold">${titulo}</h1>
                                    <span class="hover:underline"><a>${nombreUsuario}</a></span>
                                <p>${contenido}</p>
                            </div>
                        </div>
                    </div>
                </div>
            </a>
            `
}