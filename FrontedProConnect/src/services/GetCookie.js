export async function getCookie() {
    try {
        const response = await fetch("http://localhost:3000/api/usuario/getCookie", {
            method: "GET",
            credentials: "include",
        });
        if (!response.ok) {
            return null;
        } 
        const data = await response.json();
        if (data) {
            return data['user']['row'][0]
        } else {
            return null;
        }
    } catch (e) {
        console.log("Error al verificar autenticación:");
        return null;
    }
}