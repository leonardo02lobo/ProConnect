import { ObtenerEmpresa } from "../scripts/ts/Registrarse";

export async function setEmpresa() {
    try {
        const response = await fetch('http://localhost:3000/api/Empresas/CrearEmpresa', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(ObtenerEmpresa())
        })
        return response;
    } catch (error) {
        alert((error as Error).message);
    }
}