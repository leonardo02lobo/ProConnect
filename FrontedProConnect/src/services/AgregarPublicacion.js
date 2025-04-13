export async function setPublicacion(publicacion){
    try {
        console.log(publicacion)
        const response = await fetch('http://localhost:3000/api/Publicacion/CrearPublicacion', {
            method: "POST",
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(publicacion)
        })
        return response;
    } catch (e) {
        console.log("Error al verificar autenticacion");
    }
}