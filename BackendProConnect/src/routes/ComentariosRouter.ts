import Router from 'express'
import { ComentariosController } from '../controllers/ComentariosController';

const router = Router()

router.get('/ObtenerComentariosPorPublicacion/:id',ComentariosController.ObtenerComentariosPorPublicacion);

router.post('/CrearComentario',ComentariosController.CrearComentario);


export default router;