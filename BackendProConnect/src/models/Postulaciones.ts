import pool from "./database";
import { Empleo } from "./Empleo";
import { User } from "./User";

export interface Postulaciones{
    UsuarioId: number;
    EmpleoId: number;
    fecha: Date;
    usuario: User;
    empleo: Empleo;
}

export const postulacionesModel = {
    async CrearPostulacion(usuarioId: string, empleoId: string){
        const [row] = await pool.query('INSERT INTO proconnect.postulaciones(usuario_id,empleo_id) VALUES (?,?);',
            [usuarioId,empleoId]
        )
        return row
    },
    async RevisarSolicitud(UsuarioId: number,EmpleoId:number){
        const [row] = await pool.query(`
            SELECT *
            FROM proconnect.postulaciones AS post
            JOIN proconnect.usuario AS usu ON post.usuario_id = usu.id
            JOIN proconnect.empleos AS empl ON post.empleo_id = empl.id
            JOIN proconnect.empresas AS emp ON empl.id_empresa = emp.id
            WHERE post.usuario_id = ? && post.empleo_id = ?;
            `,
            [UsuarioId,EmpleoId])
        return row
    },
    async ObtenerSolicitudesByID(id: number){
        const [row] = await pool.query(`
            SELECT 
            post.usuario_id as idUsuarioPostulacion,
            post.empleo_id as idPostulacionEmpleo, 
            post.fecha as fecha_postulacion,
            usu.id as idUsuario, 
            nombre,nombre_usuario, 
            email,contrasena,
            foto_perfil, 
            foto_fondo,
            usu.puesto as puestoUsuario, 
            tipo_usuario,
            emp.id as idEmpleo, 
            id_empresa, 
            empl.puesto as puestoEmpleo,
            empl.descripcion as descripcionEmpleo, 
            Titulo, 
            empl.fecha as fechaEmpleo, 
            emp.id as idEmpresa, 
            nombre_empresa, 
            emp.descripcion as descripcionEmpresa,
            pais, 
            logo
            FROM proconnect.postulaciones AS post
            JOIN proconnect.usuario AS usu ON post.usuario_id = usu.id
            JOIN proconnect.empleos AS empl ON post.empleo_id = empl.id
            JOIN proconnect.empresas AS emp ON empl.id_empresa = emp.id
            WHERE emp.id = ?;
            `,[id])
            return row
    },
    async EliminarPostulaciones(idUsuario: number, idEmpleo: number){
        const [row] = await pool.query('DELETE FROM proconnect.postulaciones WHERE usuario_id = ? AND empleo_id = ?',
            [idUsuario,idEmpleo]
        )
        return row
    }
}

export function OrganizarPostulaciones(data: any): Postulaciones{
    return {
    UsuarioId: data['idUsuarioPostulacion'],
    EmpleoId: data['idPostulacionEmpleo'],
    fecha: data['fecha_postulacion'],
    usuario: {
        id: data['idUsuario'],
        nombre: data['nombre'],
        nombreUsuario: data['nombre_usuario'],
        email: data['email'],
        contrasena: data['contrasena'],
        fotoPerfil: data['foto_perfil'],
        fotoFondo: data['foto_fondo'],
        puesto: data['puestoUsuario'],
        tipoUsuario: data['tipo_usuario']
    },
    empleo: {
        id: data['idEmpleo'],
        idEmpresa: data['id_empresa'],
        empresa: {
            nombreEmpresa: data['nombre_empresa'],
            descripcion: data['descripcionEmpresa'],
            pais: data['pais'],
            logo: data['logo'],
            usuarioEmpresa: {
                id: 0,
                nombre: "",
                nombreUsuario: "",
                email: "",
                contrasena: "",
                fotoPerfil: "",
                fotoFondo: "",
                puesto: "",
                tipoUsuario: ""
            }
        },
        puesto: data['puestoEmpleo'],
        descripcion: data['descripcionEmpleo'],
        Titulo: data['Titulo'],
        fecha: data['fechaEmpleo']
    }
}
}