import { Comentarios, ComentariosModel, OrganizarDatosComentarios } from "../models/Comentarios";
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
        const comentarios: Comentarios[] = []
        try{
            const id: number = parseInt(req.params.id);
            if(id === 0){
                res.status(401).json({error: "Error al Verificar el Id de la publicacion"})
            }
            const result: any = await ComentariosModel.ObtenerComentariosPorPublicacion(id);
            for(const element of result){
                const comentario = await OrganizarDatosComentarios(element);
                comentarios.push(comentario)
            }
            res.status(200).json(comentarios);
        }catch(e){
            res.status(500).json({error: e})
        }
    }
}