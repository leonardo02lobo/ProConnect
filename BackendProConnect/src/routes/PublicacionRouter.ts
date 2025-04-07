import { Router } from "express";
import { publicacionController } from "../controllers/PublicacionController";

const router = Router();

router.get('/',publicacionController.ObtenerPublicaciones);
router.get('/Publicacion/:id',publicacionController.ObtenerUnaPublicacion);
router.post('/CrearPublicacion',publicacionController.CrearNuevaPublicacion);
router.post('/TodasLasPublicaciones',publicacionController.TodasLasPublicaciones);

router.get('/Publicacion/DarLike/:id',publicacionController.DarLikePublicacion);
router.get('/Publicacion/EliminarLike/:id',publicacionController.EliminarLikePublicacion)

router.get('/Publicacion/BuscarLikes/:id', publicacionController.BuscarLikePublicacion)

export default router;