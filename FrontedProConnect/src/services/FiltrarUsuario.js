export async function FiltrarUsuario(id){
    const response = await fetch(`http://localhost:3000/api/usuario/Filtrar/${id}`)

    const data = await response.json()
    return data[0]
}