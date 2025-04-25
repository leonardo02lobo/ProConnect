import pool from "./database";
import { OrganizarDatosUsuario, User, UserModel } from "./User";

export interface Amigos{
    id: number;
    usuario1ID: number;
    usuario2ID: number;
    estado: string;
    usuario1?: User;
    usuario2?: User;
}

export const AmigosModel ={
    async AgregarAmigo(amigos: Omit<Amigos,'id'>){
        const [result] = await pool.query('insert into proconnect.amigos(usuario1_id,usuario2_id,estado) values (?,?,"Pendiente");',
            [amigos.usuario1ID,amigos.usuario2ID,amigos.estado])
        return result;
    },
    async EliminarAmigo(usuario1ID: string, usuario2ID: string){
        const [result] = await pool.query('DELETE FROM proconnect.amigos WHERE usuario1_id = ? && usuario2_id = ?;',
            [usuario1ID,usuario2ID])
        return result
    },
    async BuscarUsuario(usuario1ID: string, usuario2ID: string){
        const [result] = await pool.query('SELECT * FROM proconnect.amigos WHERE usuario1_id = ? && usuario2_id = ?;',
            [usuario1ID,usuario2ID])
        return result
    },
    async BuscarNumeroSeguidoresID(idUsuario: string){
        const [row] = await pool.query('SELECT COUNT(*) FROM proconnect.amigos WHERE usuario2_id = ? && estado = "Aceptado";',
            [idUsuario,idUsuario])
        return row
    },
    async BuscarNumeroSeguidosID(idUsuario: string){
        const [row] = await pool.query('SELECT COUNT(*) FROM proconnect.amigos WHERE usuario2_id = ? && estado = "Aceptado";',
            [idUsuario])
        return row
    },
    async BuscarSolicitudesPendientes(id: string){
        const [row] = await pool.query('SELECT * FROM proconnect.amigos WHERE usuario2_id = ? && estado = "Pendiente";',
            [id])
        return row
    },
    async AceptarSolicitudModel(amigos: Amigos){
        const [row] = await pool.query('update proconnect.amigos set estado = "Aceptado" where usuario1_id = ? && usuario2_id = ?;',
            [amigos.usuario1ID,amigos.usuario2ID])
            return row
    },
    async RechazarSolicitudModel(amigos: Amigos){
        const [row] = await pool.query('delete from proconnect.amigos where usuario1_id = ? && usuario2_id = ?;',
            [amigos.usuario1ID,amigos.usuario2ID])
            return row
    },
    async SeguidoresByID(id: number){
        const [row] = await pool.query('SELECT * FROM proconnect.amigos WHERE usuario2_id = ? && estado = "Aceptado";',
        [id])
        return row
    },
    async SeguidoresByID2(id: number){
        const [row] = await pool.query('SELECT * FROM proconnect.amigos WHERE usuario2_id = ? && estado = "Aceptado";',
        [id])
        return row
    }
};

export async function OrganizarDatosAmigos(datos: any): Promise<Amigos>{
    const Usuario1Res: any = await UserModel.FiltrarUsuario(datos['usuario1_id'])
    const Usuario2Res: any = await UserModel.FiltrarUsuario(datos['usuario2_id'])
    const Usuario1 = await OrganizarDatosUsuario(Usuario1Res[0])
    const Usuario2 = await OrganizarDatosUsuario(Usuario2Res[0])
    return {
        estado: datos['estado'],
        id: datos['id'],
        usuario1ID: datos['usuario1_id'],
        usuario2ID: datos['usuario2_id'],
        usuario1: Usuario1,
        usuario2: Usuario2
    }
}