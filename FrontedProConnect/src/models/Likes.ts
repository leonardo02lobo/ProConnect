import type { PublicacionModel } from "./Publicacion";
import type { Usuario } from "./Usuario";

export interface Likes {
    id: number;
    fecha: Date;
    usuario: Usuario;
    publicacion: PublicacionModel;
}