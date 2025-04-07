import type { PublicacionModel } from "./Publicacion";
import type { Usuario } from "./Usuario";

export interface Comentarios {
    id: number;
    usuario: Usuario;
    publicacion: PublicacionModel;
    contenido: string;
    fecha: Date;
}