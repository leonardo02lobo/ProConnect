import { Request, Response } from "express";
import { Publicacion, PublicacionModel } from "../models/Publicacion";
import { UserModel } from "../models/User";
import { LikesModel } from "../models/Like";
import jwt from "jsonwebtoken";

export const publicacionController = {
    async ObtenerPublicaciones(req: Request, res: Response) {
        try {
            const publicaciones = await PublicacionModel.GetAll()
            res.status(200).json(publicaciones);
        } catch (e) {
            res.status(500).json({ error: "Error Al Leer los datos de la BD" })
        }
    },
    async ObtenerUnaPublicacion(req: Request, res: Response) {
        try {
            const id = req.params.id;
            const publicacion = await PublicacionModel.GetById(id);
            if (publicacion) {
                res.status(200).json(publicacion);
            } else {
                res.status(404).json({ error: "Publicacion no encontrada" });
            }
        }
        catch (e) {
            res.status(500).json({ error: (e as Error).message })
        }
    },
    async CrearNuevaPublicacion(req: Request, res: Response) {
        try {
            await PublicacionModel.CreatePublicacion(req.body)
            res.status(200).json({ message: "Publicacion Creada Chabal" })
        } catch (e) {
            res.status(500).json({ error: (e as Error).message })
        }
    },
    async TodasLasPublicaciones(req: Request, res: Response) {
        try {
            const result = await PublicacionModel.BuscarTodasLasPublicacionesUsuario(req.body);
            res.status(200).json(result);
        } catch (e) {
            res.status(500).json({ err: (e as Error).message })
        }
    },
    async DarLikePublicacion(req: Request, res: Response) {
        try {
            const publicacion: Publicacion = await ObtenerPublicacion(req)
            const dataUser = (req as any).user['row'][0]
            await PublicacionModel.DarLikePublicacionModel(publicacion,dataUser)
        } catch (e) {
            res.status(500).json({ error: (e as Error).message })
        }
        res.status(200).json({ message: "Like Agregado" })
    },
    async BuscarLikePublicacion(req: Request, res: Response) {
        try {
            const publicacion: Publicacion = await ObtenerPublicacion(req)
            const result = await LikesModel.BuscarLike(publicacion)
            res.status(200).json(result)
        } catch (e) {
            res.status(500).json({ error: (e as Error).message })
        }
    },
    async EliminarLikePublicacion(req: Request, res: Response) {
        try{
            const publicacion: Publicacion = await ObtenerPublicacion(req)
            const dataUser = (req as any).user['row'][0]
            const result = await LikesModel.EliminarLike(publicacion,dataUser)
            await PublicacionModel.EliminarLikePublicacion(publicacion)
            res.status(200).json(result)
        }catch(e){
            res.status(500).json({error: (e as Error).message})
        }
    }
}

async function ObtenerPublicacion(req: Request): Promise<Publicacion> {
    const publicacion: Publicacion = await PublicacionModel.GetById(req.params.id) as unknown as Publicacion
    const usuario = await UserModel.FiltrarUsuario((publicacion as any)[0]['usuario_id'])
    publicacion.usuario = usuario as unknown as Publicacion['usuario']
    return publicacion;
}