import { IniciarSesion } from "./../../services/Login";

const login = document.getElementById('login')

login.addEventListener('click', async (e) => {
    e.preventDefault()
    const response = await IniciarSesion();
    if (response) {
        window.location.href = "/";
    } else {
        throw new Error(result.error || 'Error desconocido');
    }

})