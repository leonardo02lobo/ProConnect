import pool from './database'
import bcrypt from 'bcrypt'
import jwt from "jsonwebtoken"

export interface User {
    id: number;
    nombre: string;
    nombreUsuario: string;
    email: string;
    contrasena: string;
    fotoPerfil: string;
    fotoFondo: string;
    puesto: string;
    tipoUsuario: string;
}
const saltRounds = 10
let password = ''

export const UserModel = {
    async getAll() {
        const [rows] = await pool.query('SELECT * FROM usuario');
        return rows;
    },
    async FiltrarUsuario(id: number){
        const [row] = await pool.query('SELECT * FROM usuario WHERE id = ?',[id]);
        return row;
    },
    async getUser(user: Omit<User,'id'>){
        const [row] = await pool.query('SELECT * FROM usuario WHERE nombre_usuario = ?',
            [user.nombreUsuario]
        )
        return row
    },
    async createUser(user: Omit<User, 'id'>) {
        const hashedPassword = await bcrypt.hash(user.contrasena, saltRounds);
        const result = await pool.query(
            'insert into proconnect.usuario(nombre,nombre_usuario,email,contrasena, foto_perfil,foto_fondo, puesto,tipo_usuario) values (?,?,?,?,?,?,?,?);',
            [user.nombre, user.nombreUsuario, user.email, hashedPassword, user.fotoPerfil, user.fotoFondo, user.puesto, user.tipoUsuario]
        );
        return result;
    },
    async LoginUser(user: Omit<User, 'id'>) {
        const [rowPassword]: any = await pool.query('SELECT contrasena FROM usuario where nombre = ?',
            [user.nombre]
        );
        for (const row of rowPassword as any[]) {
            password = row['contrasena']
        }
        const isMatch = await bcrypt.compare(user.contrasena, password)
        const [row] = await pool.query('SELECT * FROM usuario WHERE nombre = ? && ?;',
            [user.nombre, isMatch]
        );
        if(!row || Object.keys(row).length === 0){
            return{
                token: null,
                user: null
            }
        }
        //Generar JWT
        const token = jwt.sign({
            row
        }, process.env.JWT_SECRET || "OVoBh9p438F2UCHZwjMEvsbRG0etDikA", {
            expiresIn: '1d'
        })

        return{
            token,
            user: row
        } ;
    },
    async BuscarUsuarioNombre(nombreUsuario: string){
        const nombreConWildcards = `%${nombreUsuario}%`
        const [rows] = await pool.query('select * from proconnect.usuario where nombre_usuario like ?;',[nombreConWildcards]);
        return rows
    },
    async ValidarDatosCreacionUsuarioNombreUsuario(user: Omit<User,'id'>){
        const [row] = await pool.query('select * from proconnect.usuario where nombre_usuario = ?',
            [user.nombreUsuario]
        );
        return (row as any[]).length !== 0;
    },
    async ValidarDatosCreacionUsuarioEmail(user: User){
        const [row] = await pool.query('select * from proconnect.usuario where email = ?',
            [user.email]
        );
        return (row as any[]).length !== 0;
    },
    async RecuperarContrasena(user: Omit<User,'id'>): Promise<Boolean>{
        const hashedPassword = await bcrypt.hash(user.contrasena,saltRounds);
        const [row] = await pool.query('update proconnect.usuario set contrasena = ? where nombre_usuario = ?',
            [hashedPassword,user.nombreUsuario]
        );
        if(row === null){
            return false;
        }
        return true;
    },
    async verifyToken(token: string) {
        try {
            return jwt.verify(token, process.env.JWT_SECRET || "OVoBh9p438F2UCHZwjMEvsbRG0etDikA");
        } catch (error) {
            console.log('Token inválido o expirado');
        }
    },
    async FiltrarUsuarioNombre(name: string){
        const [row] = await pool.query('SELECT * FROM usuario WHERE nombre_usuario = ?',[name]);
        return row;
    },
    async ActualizarFotoDePerfil(fotoPerfil: string,id: number){
        const [row] = await pool.query('UPDATE proconnect.usuario SET foto_perfil = ? WHERE id = ?',
            [fotoPerfil,id]
        );
        return row;
    }
}

export function OrganizarDatosUsuario(result: any): User {
    return {
        id: result['id'],
        nombre: result['nombre'],
        nombreUsuario: result['nombre_usuario'],
        email: result['email'],
        contrasena: result['contrasena'],
        fotoPerfil: result['foto_perfil'],
        fotoFondo: result['foto_fondo'],
        puesto: result['puesto'],
        tipoUsuario: result['tipo_usuario'],
    };
}