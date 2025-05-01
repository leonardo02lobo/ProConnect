import { getCookie } from "./../../services/UsuarioService";

const data = await getCookie()

if (data === null) {
    window.location.href = window.history.back()
}
