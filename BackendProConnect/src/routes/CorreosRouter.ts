import { Router } from "express";
import { correosController } from "../controllers/CorreosController";

const router = Router()

router.post('/EnviarMensaje',correosController.EnviarCorreos)

export default router;