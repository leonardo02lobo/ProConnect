import { AddUser, ELiminarUsuario, RevisarSolictud } from "../../services/AgregarUsuario";

const ConectarUsuario = document.querySelectorAll('#ConectarUsuario')

addEventListener('load', async () => {
    const resultado = await RevisarSolictud(parseInt(location.pathname.split('/')[2]))
    if(resultado === null){
        return;
    }
    ConectarUsuario[0].innerHTML = resultado
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