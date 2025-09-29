import { ObtenerEmpleoPorId } from "../../services/EmpleosService"
import { RevisarPostulacion } from "../../services/PostulacionesService";
import { getCookie } from "../../services/UsuarioService";

const empleo = document.getElementById('mirarempleo')
const buscarEmpleo = document.getElementById("empleos-container");
const BuscadorEmpleo = document.getElementById('container-busqueda')
let volver = document.getElementById('volverEmpleo')

window.addEventListener('load', () => {
    if(window.screen.width <= 767){
        empleo.parentElement.style.display = 'none'
    }
})
window.addEventListener('resize', () =>  {
    RenderizarComponentes()
})

buscarEmpleo.addEventListener('click', async (event) => {
    const tarjeta = event.target.closest('#tarjetaEmpleo');
    if (tarjeta) {
        const id = tarjeta.getAttribute('data-id');
        await ObtenerDatos(id);
    }
});

async function ObtenerDatos(id){
    const response = await ObtenerEmpleoPorId(id)
    const dataUser = await getCookie()
    const result = await RevisarPostulacion(dataUser.id,id)
    let band = result.length !== 0 ? true : false
    RellenarInformacion(response,band)
    RenderizarComponentes()
    empleo.parentElement.style.display = 'flex'
    volver = document.getElementById('volverEmpleo')
    
    volver.addEventListener('click', () => {
        BuscadorEmpleo.style.display = 'flex'
        empleo.style.display = 'none'
        empleo.parentElement.style.display = 'none'
    })
} 

function RellenarInformacion(response,band){
    empleo.style.display = 'flex'
    const fechaFormateada = new Date(response.fecha).toLocaleDateString("es-ES", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
    });
    empleo.children[0].children[1].src = (response.empresa.logo  === "") ? "/assets/ImagenesPerfil/predeterminado.png" : response.empresa.logo
    empleo.children[0].children[2].innerHTML = response.empresa.nombreEmpresa
    empleo.children[0].children[2].href = `/MirarUsuarios/${response.empresa.usuarioEmpresa.id}`
    empleo.children[1].innerHTML = response.Titulo
    empleo.children[2].innerHTML = `${response.empresa.pais}-<span>fecha: ${fechaFormateada}</span>. Solicitudes: <span>Numero de solicitudes</span>`
    empleo.children[3].innerHTML = response.descripcion
    if(band){
        empleo.children[4].children[1].style.display = "block"
        empleo.children[4].children[0].style.display = "none"
    }else{
        empleo.children[4].children[0].innerHTML = "Postularte al trabajo"
        empleo.children[4].children[0].style.display = "block"
        empleo.children[4].children[1].style.display = "none"
    }
    empleo.children[4].children[0].href = `/Postulaciones/${response.id}`
    empleo.children[5].href = `/MirarUsuarios/${response.empresa.usuarioEmpresa.id}`
    empleo.children[5].innerHTML = "Ver perfil de la empresa"
}

function RenderizarComponentes(){
    if(window.screen.width <= 767){
        BuscadorEmpleo.style.display = 'none'
        empleo.style.padding = '10px'
        empleo.children[0].children[0].style.display = "flex"
    }else{
        BuscadorEmpleo.style.display = 'flex'
        empleo.children[0].children[0].style.display = "none"
    }
}