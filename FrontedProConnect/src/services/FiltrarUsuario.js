export async function FiltrarUsuario(id) {
    try {
        const response = await fetch(`http://localhost:3000/api/usuario/Filtrar/${id}`)

        const data = await response.json()
        return data[0]
    } catch (e) {
        return []
    }
}

export async function BuscarPorNombreUsuario(name) {
    try {
        const response = await fetch(`http://localhost:3000/api/usuario/FiltrarNombre/${name}`)

        const data = await response.json()
        return data[0]
    } catch (e) {
        return []
    }
}