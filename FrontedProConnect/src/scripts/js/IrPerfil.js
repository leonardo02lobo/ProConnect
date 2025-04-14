import { BuscarPorNombreUsuario } from "../../services/FiltrarUsuario"
import { getCookie } from "../../services/GetCookie"

const Perfil = document.getElementById('Perfil')

Perfil.addEventListener('click', async () => {
    console.log('a')
    const usuario = await getCookie()
    const DatosUser = await BuscarPorNombreUsuario(Perfil.textContent)

    try{
        if(usuario['id'] === DatosUser['id']){
            window.location.href = 'MirarPerfilUsuario'
        }else{
            window.location.href = `/MirarUsuarios/${DatosUser['id']}`
        }
    }catch(e){
        window.location.href = `/MirarUsuarios/${DatosUser['id']}`
    }
})