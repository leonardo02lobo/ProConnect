export async function CrearPostulacion(idUsuario:number,idPostulacion:number){
    try{
        const response = await fetch('http://localhost:3000/api/Postulaciones/CrearPostulacion',{
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                usuario_id: idUsuario,
                empleo_id: idPostulacion 
            })
        })
        return await response.json();
    }catch(e){
        console.log((e as Error).message)
        return null
    }
}