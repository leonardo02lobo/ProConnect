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
    },
    async ObtenerByID(req: Request, res: Response){
        try{
            const result: any = await empleosModel.getByID(req.params.id)
            if(result.length === 0){
                res.status(404).json({err: "No se ha encontrado el empleo"})
            }
            const resultado: Empleo = await OrganizarDatosEmpleo(result[0])
            res.status(200).json(resultado)
        }catch(e){
            console.log((e as Error).message)
            res.status(500).json({err: "Error interno del servidor"})
        }
    },
    async ObtenerEmpleoByPuesto(req: Request, res: Response){
        try{
            const result: any = await empleosModel.getByPuesto(req.params.puesto)
            if(result.length === 0){
                return;
            }
            const resultado: Empleo = await OrganizarDatosEmpleo(result[0])
            res.status(200).json(resultado)
        }catch(e){
            console.log((e as Error).message)
            res.status(500).json({err: "Error interno del servidor"})
        }
    },
    async BuscarEmpleoPorNombre(req: Request, res: Response){
        const Empleos: Empleo[] = []
        try{
            const result: any = await empleosModel.BuscarEmpleoPorNombre(req.params.nombre)
            for(const element of result){
                const resultado: Empleo = await OrganizarDatosEmpleo(element)
                Empleos.push(resultado)
            }
            res.status(200).json(Empleos)
        }catch(e){
            console.log((e as Error).message)
            res.status(500).json({err: "Error interno del servidor"})
        }
    }
} 