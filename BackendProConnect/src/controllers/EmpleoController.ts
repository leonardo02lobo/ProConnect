import { Request, Response } from "express";
import { Empleo, empleosModel, OrganizarDatosEmpleo } from "../models/Empleo";
import { EmpresaModel } from "../models/Empresas";

export const EmpleoController = {
    async crearEmpleo(req: Request, res: Response){
        try {
            if(req.body === null){
                res.status(400).json({err: "No se ha enviado el cuerpo de la peticion"})
            }
            const empresa: any = await EmpresaModel.ObtenerEmpresaPorId((req as any).user['row'][0].id)
            if(empresa === null){
                res.status(400).json({err: "No se ha encontrado la empresa"})
            }
            req.body.idEmpresa = empresa[0].id
            const result = await empleosModel.createEmpleo(req.body);
            res.status(201).json({message: "Empleo creado correctamente"})
        } catch (error) {
            console.log((error as Error).message)
            res.status(500).json({err: "Error interno del servidor"})
        }
    },
    async ObtenerTodosLosEmpleos(req: Request, res: Response){
        const Empleos: Empleo[] = []
        try {
            const result: any = await empleosModel.getAll()
            for(const element of result){
                const resultado: Empleo = await OrganizarDatosEmpleo(element)
                Empleos.push(resultado)
            }
            res.status(200).json(Empleos)
        } catch (error) {
            console.log((error as Error).message)
            res.status(500).json({err: "Error interno del servidor"})
        }
    }
} 