import { IniciarSesion } from "../../services/UsuarioService";
import { LoginUser } from "../ts/IniciarSesion";

const login = document.getElementById('login')

login.addEventListener('click', async (e) => {
    e.preventDefault()
    const response = await IniciarSesion(LoginUser());
    if (response) {
        window.location.href = "/";
    } else {
        throw new Error(result.error || 'Error desconocido');
    }

})