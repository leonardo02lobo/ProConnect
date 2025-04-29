import { tarjeta } from "../../modules/TarjetaPostulaciones";
import { EliminarPostulacion, EnviarMensajePositivo,EnviarMensajeNegativo, ObtenerPostulacionesByID } from "./../../services/PostulacionesService";
import { getCookie } from "./../../services/UsuarioService";

const postulaciones = document.getElementById("postulaciones");

addEventListener('load', async () => {
    const dataUser = await getCookie();
    const data = await ObtenerPostulacionesByID(dataUser.id);
    let html = '';
    data.forEach(element => {
        html += tarjeta(element.usuario.nombre, element.empleo.Titulo, element.empleo.puesto, element.usuario.email, element.usuario.puesto, element.usuario.id);
    });
    if (html === "") {
        postulaciones.innerHTML = "<p class='text-gray-600 text-2xl'>Todavia no existen postulaciones para ti. intenta mas tarde</p>";
    } else {
        postulaciones.innerHTML = html;
        const btnMensajeAceptar = document.querySelectorAll('.btn-mensajeAceptar');
        const btnMensajeRechazar = document.querySelectorAll('.btn-mensajeRechazar');
        btnMensajeAceptar.forEach((element, i) => {
            element.addEventListener('click', () => EnviarMensajeAlServidor(i,element,true,data))
        })

        btnMensajeRechazar.forEach((element, i) => {
            element.addEventListener('click',() => EnviarMensajeAlServidor(i,element,false,data))
        })
    }
});

postulaciones.addEventListener('click', async (e) => {
    try {
        const btn = e.target.closest("#toggleButton");
        const infoDiv = btn?.closest('.bg-gray-800').querySelector('#info');

        if (btn && infoDiv) {
            infoDiv.classList.toggle("hidden");
            infoDiv.classList.toggle("max-h-0");
            infoDiv.classList.toggle("max-h-90");
        }
    } catch (error) {
        console.error(error);
    }
});

async function EnviarMensajeAlServidor(i,element,band,datos) {
    const data = element.parentElement.parentElement.parentElement.parentNode.children
    const nombrePostulado = data[i].children[0].children[0].textContent.split(":")[1].trim()
    const puesto = data[i].children[0].children[1].textContent.split(":")[1].trim()
    const correoDestino = data[i].children[2].children[0].children[1].textContent.split(":")[1].trim()
    const datosUsuario = await getCookie()
    if(band){
        await EnviarMensajePositivo(puesto, correoDestino,nombrePostulado,datosUsuario.nombre.trim())
    }else{
        await EnviarMensajeNegativo(puesto, correoDestino,nombrePostulado,datosUsuario.nombre.trim())
    }
    const result = await EliminarPostulacion(data[i].children[2].children[0].children[3].href.split("/")[4], datos[i].EmpleoId)
    if (result.status === 200) {
        window.location.reload()
    } else {
        alert("Error con el envio del mensaje al postulado")
    }
}