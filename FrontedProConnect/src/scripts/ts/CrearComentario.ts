const comentar = document.getElementById('comentar') as HTMLButtonElement | null;
const contenido = document.getElementById('contenido') as HTMLInputElement | null;
import { setComentario } from "../../services/AgregarComentario";
import { MostrarMensaje } from "../js/Manejoalerta";

comentar?.addEventListener('click', async () => {
    if (!ValidarCampos()) {
        return;
    }
    const id: number = parseInt(location.pathname.split('/')[2])
    await EnviarDatos(id)
})

function ValidarCampos(): boolean | undefined {
    if (contenido?.value) {
        if (contenido?.value.length > 200) {
            alert("El contenido no puede exceder los 200 caracteres.");
            return false;
        }
    }
    else {
        alert("El contenido no puede estar vacio.");
        return false;
    }
    return true;
}

async function EnviarDatos(id: number) {
    const response = await setComentario(id);
    if(response == null){
        MostrarMensaje();
    }
    if (response?.ok) {
        if (contenido) {
            contenido.value = "";
            location.reload();
        }
    } else {
        MostrarMensaje()
    }
}