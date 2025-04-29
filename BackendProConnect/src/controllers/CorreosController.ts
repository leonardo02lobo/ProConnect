import { Request,Response } from "express";
import { DataCorreo } from "../services/correosServices";
import { MensajeNegativo, MensajePositivo } from "../settings/configurateEmail";


export const correosController = {
    async EnviarCorreosPostivo(req: Request, res: Response){
        try{
            const {puesto,nombrePostulado,nombreEmpresa,correoDestino} = req.body

            const info = await DataCorreo(puesto,MensajePositivo(nombrePostulado,puesto,nombreEmpresa),correoDestino)
            res.status(200).json({message: info})
        }catch(e){
            res.status(500).json({message: "Error del servidor"})
        }
    },
    async EnviarCorreosNegativo(req: Request, res: Response){
        try{
            const {puesto,nombrePostulado,nombreEmpresa,correoDestino} = req.body

            const info = await DataCorreo(puesto,MensajeNegativo(nombrePostulado,puesto,nombreEmpresa),correoDestino)
            res.status(200).json({message: info})
        }catch(e){
            res.status(500).json({message: "Error del servidor"})
        }
    }
}