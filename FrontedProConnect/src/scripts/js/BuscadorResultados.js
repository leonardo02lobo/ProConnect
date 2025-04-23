import { TarjetaUsuario } from "../../modules/BuscarUsuario"
import { BuscarPersona,getCookie } from "../../services/UsuarioService"

const Buscar = document.getElementById('Buscar')
const BuscarUsuarioBoton = document.getElementById('BuscarUsuario')
const listado = document.querySelector('.listado')

BuscarUsuarioBoton.addEventListener('click', async (e) => {
    listado.innerHTML = ""
    e.preventDefault()
    const response = await BuscarPersona(Buscar.value)
    const usuarioCookie = await getCookie()
    response.forEach(async (element) => {
        if(usuarioCookie.id !== element.id){
            listado.innerHTML += await TarjetaUsuario(element.fotoPerfil,element.nombreUsuario,element.id)
        }
    });
    if(listado.textContent === ""){
        listado.innerHTML = "<span class='text-white'>No existen usuarios, intentar mas tarde</span>"
    }
})
