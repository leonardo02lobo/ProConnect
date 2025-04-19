import { AmigosController } from "../controllers/AmigosController";
import { middleware } from "../middleware/middleware";
import { Router } from "express";

const router = Router()


router.post('/AgregarAmigos',middleware, AmigosController.AddFriend);

router.get('/BuscarTodosLosAmigos/:id',middleware,AmigosController.BuscarID);
router.get('/EliminarSolicitud/:id',middleware,AmigosController.DeleteFriend);
router.get('/BuscarNumeroSeguidores/:id',AmigosController.BuscarNumeroSeguidoresID);
router.get('/BuscarNumeroSeguidos/:id',AmigosController.BuscarNumeroSeguidosID);
router.get('/BuscarSeguidores/:id',AmigosController.BuscarSeguidoresbyID);
router.get('/BuscarSeguidoresByID/:id',AmigosController.BuscarSeguidoresbyID2);

router.get('/BuscarSolicitudes',middleware,AmigosController.BuscarSolicitudesByID);


router.post('/AceptarSolicitud',middleware,AmigosController.AceptarSolicitud)
router.post('/RechazarSolicitud',middleware,AmigosController.RechazarSolicitud)


export default router