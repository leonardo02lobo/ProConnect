import { UserModel, type User } from "./User";
import pool from "./database";

export interface Empresas {
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

        // Crear la empresa con el ID del usuario
        const [empresaResult] = await pool.query(
            'insert into proconnect.empresas(nombre_empresa,descripcion,pais,logo,usuario_id) values(?,?,?,?,?);',
            [empresa.nombreEmpresa, empresa.descripcion, empresa.pais, empresa.logo, idUsuario]
        );

        return empresaResult;
    }
}