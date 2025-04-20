import { OrganizarDatosUsuario, UserModel, type User } from "./User";
import pool from "./database";

export interface Empresas {
    id?: number;
    nombreEmpresa: string;
    descripcion: string;
    pais: string;
    logo: string;
    usuarioEmpresa: User;
}

export const EmpresaModel = {
    async ObtenerTodas(){
        const [rows] = await pool.query('SELECT * FROM proconnect.empresas;')
        return rows;
    },

    async CrearEmpresa(empresa: Omit<Empresas,'id'>){
        const [result] = await UserModel.createUser(empresa.usuarioEmpresa);
        const idUsuario = (result as any).insertId; // Obtener el ID del usuario creado

        if (!idUsuario) {
            throw new Error("No se pudo obtener el ID del usuario creado.");
        }
        const [empresaResult] = await pool.query(
            'insert into proconnect.empresas(nombre_empresa,descripcion,pais,logo,usuario_id) values(?,?,?,?,?);',
            [empresa.nombreEmpresa, empresa.descripcion, empresa.pais, empresa.logo, idUsuario]
        );

        return empresaResult;
    },
    async ObtenerEmpresaPorId(id: number){
        const [rows] = await pool.query('SELECT * FROM proconnect.empresas WHERE usuario_id = ?;', [id]);
        return rows;
    },
    async ObtenerEmpresaByID(id: number){
        const [rows] = await pool.query('SELECT * FROM proconnect.empresas WHERE id = ?;', [id]);
        return rows;
    },
}

export async function OrganizarDatosEmpresa(empresa: any): Promise<Empresas>{
    const usuario: any = await UserModel.FiltrarUsuario(empresa['usuario_id'])
    const resultUser: User = OrganizarDatosUsuario(usuario[0])
    return {
        id: empresa.id,
        nombreEmpresa: empresa['nombre_empresa'],
        descripcion: empresa.descripcion,
        pais: empresa.pais,
        logo: empresa.logo,
        usuarioEmpresa: resultUser
    }
}