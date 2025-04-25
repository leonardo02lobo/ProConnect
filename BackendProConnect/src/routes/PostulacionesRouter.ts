import { Router } from "express";
import { PostulacionesController } from "../controllers/PostulacionesController";

const router = Router();

router.post('/CrearPostulacion',PostulacionesController.CreatePostulacion)

export default router;