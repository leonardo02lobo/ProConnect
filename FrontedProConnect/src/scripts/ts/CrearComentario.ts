const comentar = document.getElementById('comentar') as HTMLButtonElement | null;
const contenido = document.getElementById('contenido') as HTMLInputElement | null;
import { getCookie } from "../js/GetCookie";

comentar?.addEventListener('click', async () => {
    if(!ValidarCampos()){
        return;
    }
    const id: number = parseInt(location.pathname.split('/')[2])
    await EnviarDatos(id)
})

function ValidarCampos(): boolean | undefined{
    if(contenido?.value){
        if(contenido?.value.length > 200){
            alert("El contenido no puede exceder los 200 caracteres.");
            return false;
        }
    }
    else{
        alert("El contenido no puede estar vacio.");
        return false;
    }
    return true;
}

async function EnviarDatos(id: number){
    const token = await getCookie();

    const response = await fetch('http://localhost:3000/api/Comentarios/CrearComentario', {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            contenido: contenido?.value,
            publicacion_id: id,
            usuario_id: token['id']
        })
    })
    if(response.ok){
        if(contenido){
            contenido.value = "";
            location.reload();
        }
    }
}