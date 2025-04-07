import pool from "./database";
import { Publicacion } from "./Publicacion";
import { User } from "./User";

export interface Comentarios {
    id: number;
    usuario: User;
    publicacion: Publicacion;
    contenido: string;
    fecha: Date;
}

export const ComentariosModel = {
    async CrearComentario(comentario: any){
        const [row] = await pool.query('INSERT INTO proconnect.comentarios(usuario_id,publicacion_id,contenido) VALUES (?,?,?);',
            [comentario['usuario_id'],comentario['publicacion_id'],comentario['contenido']])
        return row;
    },
    async ObtenerComentariosPorPublicacion(publicacion_id: number){
        const [rows] = await pool.query('SELECT * FROM proconnect.comentarios WHERE publicacion_id = ?;', [publicacion_id]);
        return rows;
    }
}