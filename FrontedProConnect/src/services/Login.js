import { LoginUser } from "./../scripts/ts/IniciarSesion";

export async function IniciarSesion() {
    try {
        const response = await fetch('http://localhost:3000/api/usuario/loginUser', {
            method: 'POST',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(LoginUser())
        })
        return response
    } catch (error) {
        alert(error.message);
    }
}