import { MirarEmpleo } from "../../modules/MirarEmpleoModule"
import { ObtenerEmpleosPorNombre } from "../../services/EmpleosService"

const buscarEmpleo = document.getElementById("empleos-container")
const input = document.getElementById("BuscarEmpleo")
const BtnBuscar = document.getElementById("BtnBuscar")

BtnBuscar.addEventListener("click", async () => {
    if(input.value === ""){
        buscarEmpleo.innerHTML = "<h2 class='text-center'>No se encontraron resultados</h2>"
        return
    }
    const elementos = await ObtenerEmpleosPorNombre(input.value)
    RenderizarEmpleos(elementos)
})

function RenderizarEmpleos(empleos){
    let html = ""
    empleos.forEach(element => {
        html += MirarEmpleo(element.empresa.logo, element.Titulo, element.empresa.nombreEmpresa, element.empresa.pais, element.id)
    });

    buscarEmpleo.innerHTML = html;
}