export function tarjeta(nombre, nombreEmpleo, puestoDelEmpleo, correo, puestoPostulado, idPostulado) {
    return `
        <div class="bg-gray-800 shadow-lg rounded-lg overflow-hidden w-full mt-2">
            <div class="p-4">
                <h2 class="text-xl font-bold">
                    Nombre del Postulado: ${nombre}
                </h2>
                <h3 class="text-xl font-bold">
                    Empleo a postularse: ${nombreEmpleo}
                </h3>
                <p class="text-gray-600">Puesto: ${puestoDelEmpleo}</p>
            </div>
            <div class="flex justify-between p-4">
                <div>
                    <span class="bg-green-600 text-white p-3 rounded-2xl cursor-pointer btn-mensajeAceptar">Aceptar</span>
                    <span class="bg-red-600 text-white p-3 rounded-2xl cursor-pointer btn-mensajeRechazar">Rechazar</span>
                    <a href="/Mensajes" class="bg-blue-600 text-white p-3 rounded-2xl">Enviar Mensaje</a>
                </div>
                <div>
                    <svg
                        width="24"
                        height="24"
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        stroke-width="2"
                        stroke-linecap="round"
                        stroke-linejoin="round"
                        id="toggleButton"
                        class="cursor-pointer"
                        ><path stroke="none" d="M0 0h24v24H0z" fill="none"
                        ></path><path d="M4 11l8 3l8 -3"></path></svg
                    >
                </div>
            </div>
            <div id="mensajePostulado" class="hidden transition-all duration-300 ease-in-out overflow-hidden p-4">
                <div class="flex flex-col gap-4">
                    <label for="mensaje">
                        Enviar Mensaje al postulado
                        <textarea name="mensaje" id="mensaje"  class="w-full h-30 border-black border-2 dark:border-white dark:border-2 rounded-2xl p-2"
                        placeholder="Este Campo no puede quedar vacio"></textarea>
                    </label>
                    <div>
                        <span class="bg-blue-600 text-white p-3 rounded-2xl cursor-pointer" id="EnviarMensajePostulacion">Enviar respuesta</span>
                    </div>
                </div>
            </div>
            <div
                id="info"
                class="hidden transition-all duration-300 ease-in-out overflow-hidden"
            >
                <div class="p-4 bg-gray-800 flex flex-col gap-2">
                    <h1>Datos extra del Postulado</h1>
                    <span>Correo: ${correo}</span>
                    <span>Puesto del postulado: ${puestoPostulado}</span>
                    <a href="/MirarUsuarios/${idPostulado}" class="underline">Mirar Perfil del Postulado</a>
                </div>
            </div>
        </div>
    `
}