import { LikesModel } from "./Like";
import {User, UserModel} from "./User";
import pool from "./database";
export interface Publicacion{
    id: number;
    usuario: User;
    contenido: string;
    foto: string;
    fecha: Date;
    likes: number;
    titulo: string;
}

export const PublicacionModel = {
    async GetAll(){
        const [rows] = await pool.query('SELECT * FROM proconnect.publicaciones;')
        return rows;
    },
    async CreatePublicacion(publicacion: Omit<Publicacion,'id'>){
        await pool.query('insert into proconnect.publicaciones(usuario_id,contenido,likes,foto,titulo) values (?,?,?,?,?);',
            [publicacion.usuario.id,publicacion.contenido,publicacion.likes,publicacion.foto,publicacion.titulo]
        )
    },
    async BuscarTodasLasPublicacionesUsuario(publicacionUsuario: User){
        const [rows] = await pool.query('select * from proconnect.publicaciones as pu inner join proconnect.usuario as usu where usu.id = ? AND pu.usuario_id = ? order by  fecha desc',
            [publicacionUsuario.id,publicacionUsuario.id])
        return rows;
    },
    async GetById(id: string){
        const [rows] = await pool.query('SELECT * FROM proconnect.publicaciones WHERE id = ?;', [id])
        return rows;
    },
    async DarLikePublicacionModel(publicacion: Publicacion){
        const result = await LikesModel.CrearLike(publicacion);
        if(result){
            await LikesModel.AumentarLikes(publicacion)
        }
    },
    async BuscarLikePublicacion(publicacion: Publicacion){
        const [rows] = await pool.query('SELECT * FROM proconnect.likes WHERE usuario_id = ? && publicacion_id = ?;',
            [(publicacion.usuario as any)[0]['id'],(publicacion as any)[0]['id']])
        return rows;
    },
    async EliminarLikePublicacion(publicacion: Publicacion){
        const [row] = await pool.query('UPDATE proconnect.publicaciones SET likes = likes - 1 WHERE id = ?;',
            [(publicacion as any)[0]['id']])
            return row;
    }
}