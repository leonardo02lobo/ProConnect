import type { Empresas } from "./Empresas";

export interface Empleo {
    id: number;
    idEmpresa: number;
    empresa: Empresas;
    puesto: string;
    descripcion: string;
    Titulo: string;
}