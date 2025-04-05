import {User, UserModel} from "./User";
import pool from "./database";
export interface Publicacion{
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
    }
}