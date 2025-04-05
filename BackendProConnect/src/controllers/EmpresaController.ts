import { Request,Response } from "express";
import { EmpresaModel } from "../models/Empresas";

export const EmpresaController = {
    async ObtenerEmpresas(req: Request, res: Response){
        try{
            const empresas = await EmpresaModel.ObtenerTodas();
            res.status(200).json(empresas)
        }catch(e){
            res.status(400).json('Error al Obtener los datos')
        }
    },
    async CrearEmpresa(req: Request, res: Response){
        try{
            await EmpresaModel.CrearEmpresa(req.body);
            res.status(200).json({message:"Empresa Creada"})
        }catch(e){
            res.status(400).json('Error al Obtener los datos')
        }

    }
}