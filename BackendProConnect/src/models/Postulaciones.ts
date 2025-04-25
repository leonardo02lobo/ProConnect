import pool from "./database";
import { Empleo } from "./Empleo";
import { User } from "./User";

export interface Postulaciones{
    usuario_id: number;
    empleo_id: number;
    fecha: Date;
    usuario: User;
    empleo: Empleo;
}

export const postulacionesModel = {
    async CrearPostulacion(postulacion: Postulaciones){
        const [row] = await pool.query('INSERT INTO proconnect.postulaciones(usuario_id,empleo_id) VALUES (?,?);',
            [postulacion.usuario_id,postulacion.empleo_id]
        )
        return row
    }
}