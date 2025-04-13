import { TarjetaUsuario } from "../../modules/BuscarUsuario"
import { BuscarPersona } from "../../services/Buscador"

const Buscar = document.getElementById('Buscar')
const BuscarUsuarioBoton = document.getElementById('BuscarUsuario')
const listado = document.querySelector('.listado')

BuscarUsuarioBoton.addEventListener('click', async (e) => {
    e.preventDefault()
    const response = await BuscarPersona(Buscar.value)
    response.forEach(element => {
        listado.innerHTML += TarjetaUsuario(element['foto_perfil'],element['nombre_usuario'],element['id'])
    });
})
