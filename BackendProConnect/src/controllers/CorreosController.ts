import { Request,Response } from "express";
import { DataCorreo } from "../services/correosServices";


export const correosController = {
    async EnviarCorreos(req: Request, res: Response){
        try{
            const {puesto,message,correoDestino} = req.body

            const info = await DataCorreo(puesto,message,correoDestino)
            res.status(200).json({message: info})
        }catch(e){
            res.status(500).json({message: "Error del servidor"})
        }
    }
}