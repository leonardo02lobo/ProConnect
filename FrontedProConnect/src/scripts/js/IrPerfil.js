import { BuscarPorNombreUsuario } from "../../services/FiltrarUsuario"
import { getCookie } from "../../services/GetCookie"

const Perfil = document.querySelectorAll('#Perfil')

addEventListener('load', async () => {
    Perfil.forEach(async (element) => {
        const usuario = await getCookie()
        const DatosUser = await BuscarPorNombreUsuario(element.textContent)
        try {
            if (usuario['id'] === DatosUser['id']) {
                element.href = 'MirarPerfilUsuario'
            } else {
                element.href = `/MirarUsuarios/${DatosUser['id']}`
            }
        } catch (e) {
            element.href = `/MirarUsuarios/${DatosUser['id']}`
        }
    })
})