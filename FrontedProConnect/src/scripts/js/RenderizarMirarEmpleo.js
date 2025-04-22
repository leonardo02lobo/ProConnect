import { ObtenerEmpleoPorId } from "../../services/EmpleosService"

const empleo = document.getElementById('mirarempleo')

const buscarEmpleo = document.getElementById("empleos-container");
buscarEmpleo.addEventListener('click', async (event) => {
    const tarjeta = event.target.closest('#tarjetaEmpleo');
    if (tarjeta) {
        const id = tarjeta.getAttribute('data-id');
        await ObtenerDatos(id);
    }
});

async function ObtenerDatos(id){
    const response = await ObtenerEmpleoPorId(id)
    RellenarInformacion(response)
} 

function RellenarInformacion(response){
    empleo.children[0].children[0].src = (response.empresa.logo  === "") ? "/assets/ImagenesPerfil/predeterminado.png" : response.empresa.logo
    empleo.children[0].children[1].innerHTML = response.empresa.nombreEmpresa
    empleo.children[0].children[1].href = `/MirarUsuarios/${response.empresa.usuarioEmpresa.id}`
    empleo.children[1].innerHTML = response.Titulo
    empleo.children[2].innerHTML = `${response.empresa.pais}-<span>fecha</span>. Solicitudes: <span>Numero de solicitudes</span>`
    empleo.children[3].innerHTML = response.descripcion
    empleo.children[4].href = `/MirarUsuarios/${response.empresa.usuarioEmpresa.id}`
    empleo.children[4].innerHTML = "Ver perfil de la empresa"
}