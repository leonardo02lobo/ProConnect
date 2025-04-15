import { TarjetaUsuario } from "../../modules/BuscarUsuario"
import { BuscarPersona } from "../../services/Buscador"
import { getCookie } from "../../services/GetCookie"

const Buscar = document.getElementById('Buscar')
const BuscarUsuarioBoton = document.getElementById('BuscarUsuario')
const listado = document.querySelector('.listado')

BuscarUsuarioBoton.addEventListener('click', async (e) => {
    listado.innerHTML = ""
    e.preventDefault()
    const response = await BuscarPersona(Buscar.value)
    const usuarioCookie = await getCookie()
    response.forEach(element => {
        if(usuarioCookie.id !== element.id){
            listado.innerHTML += TarjetaUsuario(element['foto_perfil'],element['nombre_usuario'],element['id'])
        }
    });
    if(listado.textContent === ""){
        listado.innerHTML = "<span class='text-white'>No existen usuarios, intentar mas tarde</span>"
    }
})
