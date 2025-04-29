import { Router } from "express";
import { correosController } from "../controllers/CorreosController";

const router = Router()

router.post('/EnviarMensajePositivo',correosController.EnviarCorreosPostivo)
router.post('/EnviarMensajeNegativo',correosController.EnviarCorreosNegativo)

export default router;