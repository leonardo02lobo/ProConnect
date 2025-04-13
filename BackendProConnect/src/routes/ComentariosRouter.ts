import Router from 'express'
import { ComentariosController } from '../controllers/ComentariosController';
import { middleware } from '../middleware/middleware';

const router = Router()

router.get('/ObtenerComentariosPorPublicacion/:id',ComentariosController.ObtenerComentariosPorPublicacion);

router.post('/CrearComentario',middleware,ComentariosController.CrearComentario);


export default router;