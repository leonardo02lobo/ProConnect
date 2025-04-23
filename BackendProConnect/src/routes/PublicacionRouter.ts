import { Router } from "express";
import { publicacionController } from "../controllers/PublicacionController";
import { middleware } from "../middleware/middleware";
import { upload } from "../settings/configurateImages";

const router = Router();

router.get('/',publicacionController.ObtenerPublicaciones);
router.get('/Publicacion/:id',publicacionController.ObtenerUnaPublicacion);
router.post('/CrearPublicacion',middleware, publicacionController.CrearNuevaPublicacion);
router.post('/TodasLasPublicaciones',publicacionController.TodasLasPublicaciones);

router.get('/Publicacion/DarLike/:id',middleware,publicacionController.DarLikePublicacion);
router.get('/Publicacion/EliminarLike/:id',middleware,publicacionController.EliminarLikePublicacion)

router.get('/Publicacion/BuscarLikes/:id', publicacionController.BuscarLikePublicacion)

router.post('/ActualizarFoto',middleware,upload.single('image'), publicacionController.ActualizarFotoPublicacion)

export default router;