import type { Usuario } from "./Usuario";

export interface PublicacionModel{
    id: number;
    usuario: Usuario;
    contenido: string;
    titulo: string;
    foto: string;
    fecha: Date;
    likes: number;
}