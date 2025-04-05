import type { Usuario } from "./Usuario";

export interface PublicacionModel{
    usuario: Usuario;
    contenido: string;
    titulo: string;
    foto: string;
    fecha: Date;
    likes: number;
}