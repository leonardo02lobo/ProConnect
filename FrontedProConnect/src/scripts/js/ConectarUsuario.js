import { AddUser, ELiminarUsuario, RevisarSolictud } from "../../services/AmigosService";

const ConectarUsuario = document.querySelectorAll('#ConectarUsuario')
const Mensaje = document.querySelectorAll('#Mensaje')

addEventListener('load', async () => {
    const resultado = await RevisarSolictud(parseInt(location.pathname.split('/')[2]))
    if(resultado === null){
        return;
    }
    if(resultado !== undefined){
        ConectarUsuario[0].innerHTML = resultado
        return;
    }
    ConectarUsuario[0].innerHTML = "Amigos"
    Mensaje[0].style.display = 'block'
})

ConectarUsuario.forEach(element => {
    element.addEventListener('click', async() => {
        if(await RevisarSolictud(parseInt(location.pathname.split('/')[2])) !== "🕧 Pendiente"){
            AddUser(parseInt(location.pathname.split('/')[2]))
        }else{
            ELiminarUsuario(parseInt(location.pathname.split('/')[2]))
        }
    })
});