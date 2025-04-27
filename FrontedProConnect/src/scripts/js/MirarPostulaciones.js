import { tarjeta } from "../../modules/TarjetaPostulaciones";
import { EnviarMensaje, ObtenerPostulacionesByID } from "./../../services/PostulacionesService";
import { getCookie } from "./../../services/UsuarioService";

const postulaciones = document.getElementById("postulaciones");
let mensaje = document.getElementById('mensaje')
let EnviarMensajePostulacion = document.getElementById('EnviarMensajePostulacion')

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
        ActivarElEnvioDeMensaje(btnMensajeAceptar);
        ActivarElEnvioDeMensaje(btnMensajeRechazar);
        mensaje = document.querySelectorAll('#mensaje')
        EnviarMensajePostulacion = document.querySelectorAll('#EnviarMensajePostulacion')
        EnviarMensajePostulacion.forEach((element,i) => {
            element.addEventListener('click', async () => {
                if(mensaje[i].value === ""){
                    alert("Tienes que dejarle un mensaje al postulado")
                    return;
                }
                const data = element.parentElement.parentElement.parentElement.parentNode.children
                const puesto = data[0].children[1].textContent.split(":")[1].replace(" ","")
                const message = mensaje[i].value
                const correoDestino = data[3].children[0].children[1].textContent.split(":")[1].replace(" ","")
    
                const result = await EnviarMensaje(puesto,message,correoDestino)
    
                if(result.status === 200){
                    window.location.reload()
                }else{
                    alert("Error con el envio del mensaje al postulado")
                }
            })
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

function ActivarElEnvioDeMensaje(btns) {
    btns.forEach((element, i) => {
        element.addEventListener('click', (e) => {
            e.stopPropagation();
            const mensajePostulado = document.querySelectorAll('#mensajePostulado');
            mensajePostulado[i].classList.toggle("hidden");
            mensajePostulado[i].classList.toggle("max-h-0");
            mensajePostulado[i].classList.toggle("max-h-90");
        });
    });
}
