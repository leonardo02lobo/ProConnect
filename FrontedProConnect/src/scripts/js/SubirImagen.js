import { SubirImagenFoto } from "../../services/UsuarioService"
const btnUpload = document.getElementById("btn-upload")
const input = document.getElementById("image")

btnUpload.addEventListener('click', async (e) => {
    e.preventDefault()

    // Validación básica
    if (!input.files || input.files.length === 0) {
        alert('Por favor selecciona una imagen');
        return;
    }

    const result = await SubirImagenFoto(input)
    if(result){
        alert("Para que los cambios tengan efecto, por favor vuelve a loguearte")
        window.location.href = "/CerrarSesion"
        window.location.href = "IniciarSesion"
    }else{
        alert("Error al subir la imagen")
    }
})