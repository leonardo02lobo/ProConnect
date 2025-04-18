import type { Usuario } from "./Usuario";

export interface Amigos{
    id: number;
    estado: string;
    usuario1: Usuario;
    usuario2: Usuario;
}