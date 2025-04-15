import { Request, Response } from "express";
import { Amigos, AmigosModel } from "../models/Amigos";

export const AmigosController = {
    async AddFriend(req: Request, res: Response) {
        console.log(req.body)
        try {
            const result = await AmigosModel.AgregarAmigo(req.body);
            res.status(200).json({ message: "Usuario Agregado en los amigos" })
        } catch (error) {
            res.status(500).json({ e: (error as Error).message })
        }
    },
    async DeleteFriend(req: Request, res: Response){
        try {
            const result: any = await AmigosModel.EliminarAmigo((req as any).user['row'][0].id, req.params.id)
            res.status(200).json({message: "Usuario Eliminado"})
        } catch (error) {
            res.status(500).json(error)
        }
    },
    async BuscarID(req: Request, res: Response) {
        try {
            const result: any = await AmigosModel.BuscarUsuario((req as any).user['row'][0].id, req.params.id)
            const amigo: Amigos = {
                id: result[0]['id'],
                usuario1ID: result[0]['usuario1_id'],
                usuario2ID: result[0]['usuario2_id'],
                estado: result[0]['estado']
            }
            res.status(200).json(amigo)
        } catch (error) {
            res.status(500).json(error)
        }
    },
    async BuscarNumeroSeguidoresID(req: Request, res: Response){
        try {
            const result = await AmigosModel.BuscarNumeroSeguidoresID(req.params.id)
            res.status(200).json(result)
        } catch (error) {
            res.status(500).json((error as Error).message)
        }
    },
    async BuscarNumeroSeguidosID(req: Request, res: Response){
        try {
            const result = await AmigosModel.BuscarNumeroSeguidosID(req.params.id)
            res.status(200).json(result)
        } catch (error) {
            res.status(500).json((error as Error).message)
        }
    }
};