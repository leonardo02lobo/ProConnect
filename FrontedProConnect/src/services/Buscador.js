export async function BuscarPersona(name){
    const response = await fetch(`http://localhost:3000/api/usuario/Buscador/${name}`,{
        method: "GET",
        headers:{
            "Content-Type": "application/json"
        },
    })
    const data = await response.json()
    return data
}