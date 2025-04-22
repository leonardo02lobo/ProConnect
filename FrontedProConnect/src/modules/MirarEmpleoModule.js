export function MirarEmpleo(imagenEmpresa, nombrePuesto, nombreEmpresa, ubicacion, id) {
    return `
    <div class="flex flex-row items-center rounded-lg w-full h-40 bg-gray-800 text-white p-4 cursor-pointer" data-id=${id} id="tarjetaEmpleo">
        <img src=${imagenEmpresa === "" ? "/assets/ImagenesPerfil/predeterminado.png" : imagenEmpresa} alt="imagenempresa" class="rounded-full w-16">
        <div class="flex flex-col ml-4">
            <h1>${nombrePuesto}</h1>
            <p class="">${nombreEmpresa}</p>
            <span>Pais: ${ubicacion}</span>
        </div>
    </div>
    `
}