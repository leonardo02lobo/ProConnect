import { Publicacion } from "./Publicacion";
import pool from "./database";
import { User } from "./User";

export interface Likes {
    id: number;
    usuario_id: number;
    publicacion_id: number;
    fecha: Date;
}

export const LikesModel = {
    async CrearLike(publicacion: Publicacion){
        const [row] = await pool.query('INSERT INTO proconnect.likes(usuario_id,publicacion_id) VALUES (?,?);',
            [(publicacion.usuario as any)[0]['id'],(publicacion as any)[0]['id']])
        return row;
    },
    async AumentarLikes(publicacion: Publicacion){
        const [row] = await pool.query('UPDATE proconnect.publicaciones SET likes = likes + 1 WHERE id = ?;',
            [(publicacion as any)[0]['id']])
        return row;
    },
    async BuscarLike(publicacion: Publicacion){
        const [row] = await pool.query('SELECT * FROM proconnect.likes WHERE usuario_id = ? && publicacion_id = ?;',
            [(publicacion.usuario as any)[0]['id'],(publicacion as any)[0]['id']])
        return row;
    },
    async EliminarLike(publicacion: Publicacion){
        const [row] = await pool.query('DELETE FROM proconnect.likes WHERE usuario_id = ? && publicacion_id = ?;',
            [(publicacion.usuario as any)[0]['id'],(publicacion as any)[0]['id']])
        return row;
    },
}