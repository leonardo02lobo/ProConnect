import { AmigosController } from "../controllers/AmigosController";
import { middleware } from "../middleware/middleware";
import { Router } from "express";

const router = Router()


router.post('/AgregarAmigos',middleware, AmigosController.AddFriend);

router.get('/BuscarTodosLosAmigos/:id',middleware,AmigosController.BuscarID);
router.get('/EliminarSolicitud/:id',middleware,AmigosController.DeleteFriend);
router.get('/BuscarNumeroSeguidores/:id',AmigosController.BuscarNumeroSeguidoresID)
router.get('/BuscarNumeroSeguidos/:id',AmigosController.BuscarNumeroSeguidosID)

router.get('/BuscarSolicitudes',middleware,AmigosController.BuscarSolicitudesByID);


export default router