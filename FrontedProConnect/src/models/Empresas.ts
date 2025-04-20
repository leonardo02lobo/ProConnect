import type { Usuario } from "./Usuario";

export interface Empresas {
    id?: number;
    nombreEmpresa: string;
    descripcion: string;
    pais: string;
    logo: string;
    usuarioEmpresa: Usuario;
}