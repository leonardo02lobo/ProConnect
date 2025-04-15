import pool from "./database";

export interface Amigos{
    id: number;
    usuario1ID: number;
    usuario2ID: number;
    estado: string;
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
        const [row] = await pool.query('SELECT COUNT(*) FROM proconnect.amigos WHERE usuario1_id = ? && (estado = "Pendiente" OR estado = "seguidor");',
            [idUsuario])
        return row
    },
    async BuscarNumeroSeguidosID(idUsuario: string){
        const [row] = await pool.query('SELECT COUNT(*) FROM proconnect.amigos WHERE usuario1_id = ? && estado = "seguidor";',
            [idUsuario])
        return row
    }
};