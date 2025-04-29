import type { Postulaciones } from "../models/Postulaciones";

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

export async function RevisarPostulacion(idUsuario: number, idPostulacion: number){
    try {
        const response = await fetch('http://localhost:3000/api/Postulaciones/RevisarSolicitudEmpleo',{
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
    } catch (error) {
        console.log((error as Error).message)
        return null
    }
}

export async function ObtenerPostulacionesByID(id: number): Promise<Postulaciones[] | null>{
    try {
        const response = await fetch(`http://localhost:3000/api/Postulaciones/ObtenerSolicitudes/${id}`)

        if(!response.ok){
            return null;
        }
        const data: Postulaciones[] = await response.json();
        return data
    } catch (error) {
        console.log((error as Error).message)
        return null;
    }
}

export async function EnviarMensajePositivo(puesto: string, correoDestino:string,nombrePostulado: string,nombreEmpresa: string){
        const response = await fetch('http://localhost:3000/api/Correos/EnviarMensajePositivo',{
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                puesto: puesto,
                nombrePostulado: nombrePostulado,
                nombreEmpresa: nombreEmpresa,
                correoDestino: correoDestino
            })
        })
        return response;
}

export async function EnviarMensajeNegativo(puesto: string, correoDestino:string,nombrePostulado: string,nombreEmpresa: string){
    const response = await fetch('http://localhost:3000/api/Correos/EnviarMensajeNegativo',{
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            puesto: puesto,
            nombrePostulado: nombrePostulado,
            nombreEmpresa: nombreEmpresa,
            correoDestino: correoDestino
        })
    })
    return response;
}

export async function EliminarPostulacion(idUsuario: number, idEmpleo:number){
    const response = await fetch('http://localhost:3000/api/Postulaciones/EliminarPostulacion',{
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            usuario_id: idUsuario,
            empleo_id: idEmpleo
        })
    })
    return response
}