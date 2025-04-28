import { Router } from "express";
import { PostulacionesController } from "../controllers/PostulacionesController";

const router = Router();

router.post('/CrearPostulacion',PostulacionesController.CreatePostulacion)

router.post('/RevisarSolicitudEmpleo',PostulacionesController.RevisarSolicitud)

router.get('/ObtenerSolicitudes/:id',PostulacionesController.ObtenerPostulacionesByID)

router.post('/EliminarPostulacion',PostulacionesController.EliminarPostulacion)

export default router;