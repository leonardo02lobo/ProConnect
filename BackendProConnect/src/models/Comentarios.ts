import pool from "./database";
import { OrganizarDatosPublicacion, Publicacion, PublicacionModel } from "./Publicacion";
import { OrganizarDatosUsuario, User, UserModel } from "./User";

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

export async function OrganizarDatosComentarios(data: any): Promise<Comentarios>{
    const resultUser: any = await UserModel.FiltrarUsuario(data['usuario_id'])
    const resultPublicacion: any = await PublicacionModel.GetById(data['publicacion_id'])
    const DatosPublicacion = await OrganizarDatosPublicacion(resultPublicacion[0])
    const DatosUsuario = await OrganizarDatosUsuario(resultUser[0])
    return {
        id: data['id'],
        usuario: DatosUsuario,
        publicacion: DatosPublicacion,
        contenido: data['contenido'],
        fecha: new Date(data['fecha'])
    }
}