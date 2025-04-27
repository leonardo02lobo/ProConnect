import { MirarEmpleo } from "../../modules/MirarEmpleoModule"
import { ObtenerEmpleos, ObtenerEmpleosPorNombre } from "../../services/EmpleosService"

const buscarEmpleo = document.getElementById("empleos-container")
const input = document.getElementById("BuscarEmpleo")
const BtnBuscar = document.getElementById("BtnBuscar")

BtnBuscar.addEventListener("click", async () => {
    if(input.value === ""){
        const elementos = await ObtenerEmpleos()
        RenderizarEmpleos(elementos)
        return
    }
    const elementos = await ObtenerEmpleosPorNombre(input.value)
    if(elementos.length === 0){
        buscarEmpleo.innerHTML = "<h2 class='text-center'>No se encontraron resultados</h2>"
        return
    }
    RenderizarEmpleos(elementos)
})

function RenderizarEmpleos(empleos){
    let html = ""
    empleos.forEach(element => {
        html += MirarEmpleo(element.empresa.logo, element.Titulo, element.empresa.nombreEmpresa, element.empresa.pais, element.id)
    });

    buscarEmpleo.innerHTML = html;
}