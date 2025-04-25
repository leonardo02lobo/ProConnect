import { Request, Response } from "express"
import { postulacionesModel } from "../models/Postulaciones"

export const PostulacionesController = {
    async CreatePostulacion(req: Request, res: Response){
        try {
            if(req.body === null){
                res.status(400).json({error: "No se tiene datos en el Body"})
            }
            const result = await postulacionesModel.CrearPostulacion(req.body)
            res.status(200).json({message: "Se creo la postulacion"})
        } catch (error) {
            res.status(500).json({mesagge: "Error del servidor"})
        }
    }
}