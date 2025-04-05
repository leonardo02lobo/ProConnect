import type { Usuario } from "./Usuario";

export interface Empresas {
    nombreEmpresa: string;
    descripcion: string;
    pais: string;
    logo: string;
    usuarioEmpresa: Usuario;
}