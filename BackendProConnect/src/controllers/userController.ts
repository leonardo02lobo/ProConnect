import { Request, Response } from 'express';
import { OrganizarDatosUsuario, User, UserModel } from '../models/User';

export const UserController = {
    async getUser(req: Request, res: Response) {
        try {
            const user = await UserModel.getAll() 
            res.status(200).json(user)
        } catch (e) {
            res.status(500).json({ e: 'Error al obtener los usuarios' })
        }
    },
    async ObtenerUsuarioID(req: Request, res: Response) {
        try {
            const id: number = parseInt(req.params.id)
            const result = await UserModel.FiltrarUsuario(id);
            res.status(200).json(result)
        } catch (e) {
            res.status(500).json({ er: (e as Error).message })
        }
    },
    async getFindUser(req: Request, res: Response) {
        try {
            const FindUser:any = await UserModel.getUser(req.body)
            const usuario: User = OrganizarDatosUsuario(FindUser[0]);
            res.status(200).json(usuario);
        } catch (e) {
            res.status(400).json({ e: "Error al ejecutar la accion" })
        }
    },
    async SetUser(req: Request, res: Response) {
        try {
            const result = await UserModel.createUser(req.body);
            res.status(201).json({ message: "Usuario Agregado" })
        } catch (e) {
            res.status(400).json({ e: "Error al crear el usuario" })
        }
    },
    async getFindOneUSer(req: Request, res: Response) {
        try {
            const { token, user } = await UserModel.LoginUser(req.body);
            if(token === null || user === null){
                res.status(401).json({e: "No se pudo iniciar Sesion"})
                return;
            }
            // Configurar cookie segura
            res.cookie('authToken', token, {
                httpOnly: true,
                secure: process.env.NODE_ENV === 'production',
                sameSite: 'lax',
                maxAge: 24 * 60 * 60 * 1000,
                path: '/',
            });

            res.status(200).json({
                token,
                message: "Inicio de sesión exitoso",
            });
        } catch (e: any) {
            console.error("Error en login:", e);
            res.status(401).json({ error: e.message || "Error al autenticar al usuario" });
        }
    },
    async setPassword(req: Request, res: Response) {
        try {
            const isPassword: Boolean = await UserModel.RecuperarContrasena(req.body);
            if (isPassword) {
                res.status(200).json({ message: "La Contraseña fue restablecida" })
            } else {
                res.status(400).send({ e: "Error al cambiar la contraseña" })
            }
        } catch (e) {
            res.status(500).send({ e: "Error al cambiar la contraseña por lado del servidor" })
        }
    },
    async CerrarSesion(req: Request, res: Response) {
        try {
            res.clearCookie('authToken', {
                httpOnly: true,
                secure: process.env.NODE_ENV === 'production',
                sameSite: 'lax',
                path: '/',
            });
            res.status(200).json({ message: "Sesion cerrada exitosamente" });
        } catch (error) {
            res.status(500).json({message: "Error en el servidor"})
        }
    },
    async Buscador(req: Request, res: Response) {
        const usuario: User[] = []
        try {
            const result: any = await UserModel.BuscarUsuarioNombre(req.params.nombreUsuario)
            result.forEach((element: User) => {
                usuario.push(OrganizarDatosUsuario(element));
            });
            res.status(200).json(usuario)
        } catch (e) {
            res.status(400).json({ e: (e as Error).message })
        }
    },
    async ValidarDatosCrearUsuarioNombreUsuario(req: Request, res: Response) {
        try {
            console.log(req.body)
            const result = await UserModel.ValidarDatosCreacionUsuarioNombreUsuario(req.body);
            res.status(200).json(result)
        } catch (e) {
            res.status(401).json({ e: (e as Error).message })
        }
    },
    async ValidarDatosCrearUsuarioEmail(req: Request, res: Response) {
        try {
            const result = await UserModel.ValidarDatosCreacionUsuarioEmail(req.body);
            res.status(200).json(result)
        } catch (e) {
            res.status(401).json({ e: (e as Error).message })
        }
    },
    async VerificarSesion(req: Request, res: Response) {
        try {
            const token = req.cookies.authToken || req.headers.authorization?.split(' ')[1];
            if (!token) {
                res.status(401).json({ isAuthenticated: false });
            }
            const decoded:any = await UserModel.verifyToken(token);
           const User = OrganizarDatosUsuario(decoded['row'][0])
            res.status(200).json({ isAuthenticated: true, user: User });
        } catch (e) {
            console.log('Token Invalido');
        }
    },
    async BuscarUsuarioNombre(req: Request, res: Response){
        const usuarios: User[] = []
        try{
            const resultado: any = await UserModel.BuscarUsuarioNombre(req.params.nombre)
            resultado.forEach((element: any) => {
                usuarios.push(OrganizarDatosUsuario(element))
            });
            res.status(200).json(usuarios)
        }catch(e){
            res.status(500).json({error: e})
        }
    }
}