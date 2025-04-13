export async function DatosUsuario(){
    try {
        const response = await fetch("http://localhost:3000/api/usuario/getCookie", {
            method: "GET",
            credentials: "include",
        });
        const data = await response.json();
        return data
    } catch (e) {
        console.log("Error al verificar autenticación:");
    }
}

export async function ObtenerPublicacionesUsuario(usuario){
    try{
        const response = await fetch("http://localhost:3000/api/Publicacion/TodasLasPublicaciones",{
            method: "POST",
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(usuario)
        });
        const data = await response.json();
        return data;
    }catch(e){
        console.log(e.message)
        console.log("Error al intentar obtener la publicacion")
    }
}