import { Request, Response } from "express"
import {OrganizarPostulaciones, Postulaciones, postulacionesModel } from "../models/Postulaciones"
import { EmpresaModel } from "../models/Empresas"

export const PostulacionesController = {
    async CreatePostulacion(req: Request, res: Response){
        try {
            if(req.body === null){
                res.status(400).json({error: "No se tiene datos en el Body"})
            }
            const result = await postulacionesModel.CrearPostulacion(req.body['usuario_id'],req.body['empleo_id'])
            res.status(200).json({message: "Se creo la postulacion"})
        } catch (error) {
            console.log((error as Error).message)
            res.status(500).json({mesagge: "Error del servidor"})
        }
    },
    async RevisarSolicitud(req: Request, res: Response){
        try{
            if(req.body === null){
                res.status(400).json({error: "No se tiene datos en el Body"})
            }
            const result = await postulacionesModel.RevisarSolicitud(req.body['usuario_id'],req.body['empleo_id'])
            res.status(200).json(result)
        }catch(e){
            console.log((e as Error).message)
            res.status(500).json({message: "Error del servidor"})
        }
    },
    async ObtenerPostulacionesByID(req: Request, res: Response){
        const postulaciones: Postulaciones[] = []
        try {
            const id: number = parseInt(req.params.id)
            if(id === 0){
                res.status(401).json({message: "Datos Erroneos en el Body"})
            }
            const dataUser: any = await EmpresaModel.ObtenerEmpresaPorId(id)
            const result:any = await postulacionesModel.ObtenerSolicitudesByID(dataUser[0].id)
            for(const res of result){
                const datosOrganizados = OrganizarPostulaciones(res)
                postulaciones.push(datosOrganizados)
            }
            res.status(200).json(postulaciones)
        } catch (error) {
            res.status(500).json({message: "Error del servidor"})
        }
    }
}