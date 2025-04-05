import { LoginUser } from "../ts/IniciarSesion";

const login = document.getElementById('login')

login.addEventListener('click',async (e) => {
    e.preventDefault()
    try {
        const response = await fetch('http://localhost:3000/api/usuario/loginUser', {
            method: 'POST',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(LoginUser())
        })
        if (response.ok) {
            window.location.href = "/";
        } else {
            throw new Error(result.error || 'Error desconocido');
        }
    } catch (error) {
        console.error('Error:', error);
        alert(error.message);
    }
})