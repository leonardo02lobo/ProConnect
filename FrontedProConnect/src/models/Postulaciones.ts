import type { Empleo } from "./Empleo";
import type { Usuario } from "./Usuario";

export interface Postulaciones{
    UsuarioId: number;
    EmpleoId: number;
    fecha: Date;
    usuario: Usuario;
    empleo: Empleo;
}
