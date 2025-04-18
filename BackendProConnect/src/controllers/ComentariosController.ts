import { Comentarios, ComentariosModel } from "../models/Comentarios";
import { Request, Response } from "express";

export const ComentariosController = {
    async CrearComentario(req: Request, res: Response){
        try{
            const result = ComentariosModel.CrearComentario(req.body as Comentarios);
            res.status(200).json({message: "Comentario Creado"})
        }catch(e){
            res.status(500).json({error: e})
        }
    },
    async ObtenerComentariosPorPublicacion(req: Request, res: Response){
        try{
            const id: number = parseInt(req.params.id);
            if(id === 0){
                res.status(401).json({error: "Error al Verificar el Id de la publicacion"})
            }
            const result = await ComentariosModel.ObtenerComentariosPorPublicacion(id);
            res.status(200).json(result);
        }catch(e){
            res.status(500).json({error: e})
        }
    }
}