import { Router } from "express";
import { publicacionController } from "../controllers/PublicacionController";

const router = Router();

router.get('/',publicacionController.ObtenerPublicaciones);
router.get('/Publicacion/:id',publicacionController.ObtenerUnaPublicacion);
router.post('/CrearPublicacion',publicacionController.CrearNuevaPublicacion);
router.post('/TodasLasPublicaciones',publicacionController.TodasLasPublicaciones);

export default router;