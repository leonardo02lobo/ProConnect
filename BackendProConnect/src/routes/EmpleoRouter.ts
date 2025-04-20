import { Router } from "express";
import { EmpleoController } from "../controllers/EmpleoController";
import { middleware } from "../middleware/middleware";

const router = Router()

router.post('/crearEmpleo',middleware, EmpleoController.crearEmpleo)
router.get('/ObtenerTodosLosEmpleos',EmpleoController.ObtenerTodosLosEmpleos)


export default router;