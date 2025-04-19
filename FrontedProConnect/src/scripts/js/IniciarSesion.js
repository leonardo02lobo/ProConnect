import { IniciarSesion } from "../../services/UsuarioService";
import { LoginUser } from "../ts/IniciarSesion";

const login = document.getElementById('login')

login.addEventListener('click', async (e) => {
    e.preventDefault()
    const response = await IniciarSesion(LoginUser());
    if (response.ok) {
        window.location.href = "/";
    } else {
        alert("Error al iniciar sesión. Verifica tus credenciales.");
        window.location.reload();
    }

})