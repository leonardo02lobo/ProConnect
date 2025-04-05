import { Request, Response } from "express";
import { PublicacionModel } from "../models/Publicacion";

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
    async CrearNuevaPublicacion(req:Request, res: Response){
        try{
            await PublicacionModel.CreatePublicacion(req.body)
            res.status(200).json({message: "Publicacion Creada Chabal"})
        }catch(e){
            res.status(500).json({ error: (e as Error).message })
        }
    },
    async TodasLasPublicaciones(req: Request, res: Response){
        try{
            const result = await PublicacionModel.BuscarTodasLasPublicacionesUsuario(req.body);
            res.status(200).json(result);
        }catch(e){
            res.status(500).json({err: (e as Error).message})
        }
    }
}