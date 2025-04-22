import type { Empleo } from "../models/Empleo";

export async function crearEmpleoEndopint(empleo: Empleo) {
    try {
        const response = await fetch('http://localhost:3000/api/Empleo/crearEmpleo', {
            method: "POST",
            credentials: "include",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(empleo)
        });
        if (!response.ok) {
            console.log("Error al crear el empleo")
            return;
        }
        const data = await response.json();
        return data;
    } catch (error) {
        console.log((error as Error).message)
    }
}

export async function ObtenerEmpleos(): Promise<Empleo[]> {
    try {
        const response = await fetch('http://localhost:3000/api/Empleo/ObtenerTodosLosEmpleos', {
            method: "GET",
            headers: {
                "Content-Type": "application/json"
            }
        })
        if(!response.ok){
            console.log("error al obtener los empleos")
            return []
        }
        const data: Empleo[] = await response.json()
        return data
    } catch (error) {
        console.log((error as Error).message)
        return []
    }
}

export async function ObtenerEmpleoPorId(id: string): Promise<Empleo | null> {
    try {
        const response = await fetch(`http://localhost:3000/api/Empleo/ObtenerByID/${id}`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json"
            }
        })
        if(!response.ok){
            console.log("error al obtener el empleo")
            return null
        }
        const data: Empleo = await response.json()
        return data
    } catch (error) {
        console.log((error as Error).message)
        return null
    }

}

export async function ObtenerDatosPorPuesto(puesto: string): Promise<Empleo[] | null>{
    try {
        const response = await fetch(`http://localhost:3000/api/Empleo/ObtenerEmpleoByPuesto/${puesto}`)
        if(!response.ok){
            console.log("error al obtener los empleos por puesto")
            return null
        }
        const data: Empleo[] = await response.json()
        return data
    } catch (error) {
        console.log((error as Error).message)
        return null
    }
}

export async function ObtenerEmpleosPorNombre(nombre: string): Promise<Empleo[] | null> {
    try {
        const response = await fetch(`http://localhost:3000/api/Empleo/BuscarEmpleoPorNombre/${nombre}`)
        if(!response.ok){
            console.log("error al obtener los empleos por nombre")
            return null
        }
        const data: Empleo[] = await response.json()
        return data
    } catch (error) {
        console.log((error as Error).message)
        return null
    }
}