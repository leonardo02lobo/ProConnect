import { Router } from "express";
import { EmpleoController } from "../controllers/EmpleoController";
import { middleware } from "../middleware/middleware";

const router = Router()

router.post('/crearEmpleo',middleware, EmpleoController.crearEmpleo)
router.get('/ObtenerTodosLosEmpleos',EmpleoController.ObtenerTodosLosEmpleos)
router.get('/ObtenerByID/:id',EmpleoController.ObtenerByID)
router.get('/ObtenerEmpleoByPuesto/:puesto',EmpleoController.ObtenerEmpleoByPuesto)
router.get('/BuscarEmpleoPorNombre/:nombre',EmpleoController.BuscarEmpleoPorNombre)


export default router;