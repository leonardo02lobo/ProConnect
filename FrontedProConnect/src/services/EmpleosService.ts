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
        console.log(data)
        return data
    } catch (error) {
        console.log((error as Error).message)
        return []
    }
}