import type { Empleo } from "../../models/Empleo";
import { crearEmpleoEndopint } from "../../services/EmpleosService";

const formularioEmpleo = document.getElementById("formularioEmpleo") as HTMLFormElement | null;
const crearEmpleo = document.getElementById("crearEmpleo") as HTMLButtonElement | null;

crearEmpleo?.addEventListener("click",async (e) => {
    e.preventDefault();
    const empleo: Empleo | void = ValidarFormulario();
    if(!empleo){
        alert("Error al crear el empleo")
        return;    
    }
    const result = await crearEmpleoEndopint(empleo)
    
    if(result){
        window.location.href = "/MirarPerfilUsuario"
    }else{
        alert("Error al crear el empleo")
    }
})

function ValidarFormulario(): Empleo | void{
    if (!formularioEmpleo) {
        alert("Formulario no encontrado")
        return;
    }

    if(formularioEmpleo?.titulo == null || formularioEmpleo?.titulo.value == ""){
        alert("El campo titulo es obligatorio")
        return;
    }
    if(formularioEmpleo.puesto == null || formularioEmpleo.puesto.value == ""){
        alert("El campo puesto es obligatorio")
        return;
    }
    if(formularioEmpleo.descripcion == null || formularioEmpleo.descripcion.value == ""){
        alert("El campo descripcion es obligatorio")
        return;
    }
    const empleo: Empleo = {
        id: 0,
        idEmpresa: 0,
        empresa: {
            nombreEmpresa: "",
            descripcion: "",
            pais: "",
            logo: "",
            usuarioEmpresa: {
                id: 0,
                nombre: "",
                nombreUsuario: "",
                email: "",
                contrasena: "",
                fotoPerfil: "",
                fotoFondo: "",
                puesto: "",
                tipoUsuario: ""
            }
        },
        puesto: formularioEmpleo.puesto.value,
        descripcion: formularioEmpleo.descripcion.value,
        Titulo: formularioEmpleo.titulo.value
    }
    return empleo;
}