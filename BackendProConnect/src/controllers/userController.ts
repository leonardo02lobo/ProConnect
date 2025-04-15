import { Request, Response } from 'express';
import { UserModel } from '../models/User';

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
            const id = req.params.id
            const result = await UserModel.FiltrarUsuario(Number.parseInt(id));
            res.status(200).json(result)
        } catch (e) {
            res.status(500).json({ er: (e as Error).message })
        }
    },
    async getFindUser(req: Request, res: Response) {
        try {
            const FindUser = await UserModel.getUser(req.body)
            res.status(200).json(FindUser);
        } catch (e) {
            res.status(400).json({ e: "Error al ejecutar la accion" })
        }
    },
    async SetUser(req: Request, res: Response) {
        try {
            console.log(req.body)
            const result = await UserModel.createUser(req.body);
            console.log(result)
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
            res.status(400).send({ e: "Error al cambiar la contraseña" })
        }
    },
    async CerrarSesion(req: Request, res: Response) {
        res.clearCookie('authToken', {
            httpOnly: true,
            secure: process.env.NODE_ENV === 'production',
            sameSite: 'lax',
            path: '/',
        });
        res.status(200).json({ message: "Sesion cerrada exitosamente" });
    },
    async Buscador(req: Request, res: Response) {
        try {
            const result = await UserModel.BuscarUsuarioNombre(req.params.nombreUsuario)
            res.status(200).json(result)
        } catch (e) {
            res.status(400).json({ e: (e as Error).message })
        }
    },
    async ValidarDatosCrearUsuarioNombreUsuario(req: Request, res: Response) {
        try {
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
            const decoded = await UserModel.verifyToken(token);
            res.status(200).json({ isAuthenticated: true, user: decoded });
        } catch (e) {
            console.log('Token Invalido');
        }
    },
    async BuscarUsuarioNombre(req: Request, res: Response){
        try{
            const resultado = await UserModel.BuscarUsuarioNombre(req.params.nombre)
            res.status(200).json(resultado)
        }catch(e){
            res.status(500).json({error: e})
        }
    }
}