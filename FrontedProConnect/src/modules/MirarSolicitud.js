export function Solicitud(nombre,fotoperfil,id){
    return `
        <div class="bg-gray-500 rounded-2xl p-3 flex flex-col gap-4">
            <div class="flex flex-row gap-3">
                <img src=${(fotoperfil !== "")? fotoperfil : "/assets/ImagenesPerfil/predeterminado.png"} class="rounded-3xl w-10 h-10" alt="">
                <h1 class="text-black hover:underline"><a href="/MirarUsuarios/${id}">${nombre}</a></h1>
            </div>
            <div class="flex flex-row gap-3">
                <a class="bg-blue-700 text-white rounded-2xl p-2 cursor-pointer" id="aceptar">✔️ Aceptar</a>
                <a class="bg-blue-700 text-white rounded-2xl p-2 cursor-pointer" id="rechazar">⛔ Rechazar</a>
            </div>
        </div>
    `
}