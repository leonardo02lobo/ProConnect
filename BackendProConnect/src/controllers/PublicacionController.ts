import { Request, Response } from "express";
import { OrganizarDatosPublicacion, Publicacion, PublicacionModel } from "../models/Publicacion";
import { UserModel } from "../models/User";
import { LikesModel } from "../models/Like";

export const publicacionController = {
    async ObtenerPublicaciones(req: Request, res: Response) {
        const Publicaciones: Publicacion[] = []
        try {
            const publicaciones: any = await PublicacionModel.GetAll()
            for (const element of publicaciones) {
                const elementos: Publicacion = await OrganizarDatosPublicacion(element);
                Publicaciones.push(elementos);
            }
            res.status(200).json(Publicaciones);
        } catch (e) {
            res.status(500).json({ error: "Error Al Leer los datos de la BD" })
        }
    },
    async ObtenerUnaPublicacion(req: Request, res: Response) {
        try {
            const id = req.params.id;
            const publicacion: any = await PublicacionModel.GetById(id);
            const PublicacionOrganizada: Publicacion = await OrganizarDatosPublicacion(publicacion[0]);
            if (PublicacionOrganizada !== null) {
                res.status(200).json(PublicacionOrganizada);
            } else {
                res.status(404).json({ error: "Publicacion no encontrada" });
            }
        }
        catch (e) {
            res.status(500).json({ error: (e as Error).message })
        }
    },
    async CrearNuevaPublicacion(req: Request, res: Response) {
        try {
            await PublicacionModel.CreatePublicacion(req.body)
            res.status(200).json({ message: "Publicacion Creada Chabal" })
        } catch (e) {
            res.status(500).json({ error: (e as Error).message })
        }
    },
    async TodasLasPublicaciones(req: Request, res: Response) {
        const Publicaciones: Publicacion[] = []
        try {
            const result:any = await PublicacionModel.BuscarTodasLasPublicacionesUsuario(req.body);
            for(const element of result){
                const elementos: Publicacion = await OrganizarDatosPublicacion(element);
                Publicaciones.push(elementos)
            }
            res.status(200).json(Publicaciones);
        } catch (e) {
            res.status(500).json({ err: (e as Error).message })
        }
    },
    async DarLikePublicacion(req: Request, res: Response) {
        try {
            const publicacion: Publicacion = await ObtenerPublicacion(req)
            const dataUser = (req as any).user['row'][0]
            if(dataUser === null || publicacion === null){
                res.status(401).json({error: "Error por datos nulos"});
            }
            await PublicacionModel.DarLikePublicacionModel(publicacion,dataUser)
        } catch (e) {
            res.status(500).json({ error: (e as Error).message })
        }
        res.status(200).json({ message: "Like Agregado" })
    },
    async BuscarLikePublicacion(req: Request, res: Response) {
        try {
            const publicacion: Publicacion = await ObtenerPublicacion(req)
            if(publicacion === null){
                res.status(401).json({error: "Error por datos nulos"});
            }
            const result = await LikesModel.BuscarLike(publicacion)
            res.status(200).json(result)
        } catch (e) {
            res.status(500).json({ error: (e as Error).message })
        }
    },
    async EliminarLikePublicacion(req: Request, res: Response) {
        try{
            const publicacion: Publicacion = await ObtenerPublicacion(req)
            const dataUser = (req as any).user['row'][0]
            if(publicacion === null || dataUser === null){
                res.status(401).json({error: "Error por datos nulos"});
            }
            const result = await LikesModel.EliminarLike(publicacion,dataUser)
            await PublicacionModel.EliminarLikePublicacion(publicacion)
            res.status(200).json(result)
        }catch(e){
            res.status(500).json({error: (e as Error).message})
        }
    }
}

async function ObtenerPublicacion(req: Request): Promise<Publicacion> {
    const publicacion: Publicacion = await PublicacionModel.GetById(req.params.id) as unknown as Publicacion
    const usuario = await UserModel.FiltrarUsuario((publicacion as any)[0]['usuario_id'])
    publicacion.usuario = usuario as unknown as Publicacion['usuario']
    return publicacion;
}