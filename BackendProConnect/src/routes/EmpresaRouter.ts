import { Router } from "express";
import { EmpresaController } from "../controllers/EmpresaController";


const router = Router();

router.get('/',EmpresaController.ObtenerEmpresas);
router.post('/CrearEmpresa',EmpresaController.CrearEmpresa);

export default router;