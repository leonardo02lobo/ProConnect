import { Router } from "express";
import { UserController } from "../controllers/userController";
import { middleware } from "../middleware/middleware";

const router = Router();

router.get('/',UserController.getUser);
router.get('/Buscador/:nombreUsuario',UserController.Buscador);
router.get('/Filtrar/:id',UserController.ObtenerUsuarioID);

router.post('/crearUsuario',UserController.SetUser);
router.post('/ValidarNombreUsuario',UserController.ValidarDatosCrearUsuarioNombreUsuario);
router.post('/ValidarEmail',UserController.ValidarDatosCrearUsuarioEmail);

router.post('/loginUser',UserController.getFindOneUSer);
router.post('/SetPassword',UserController.setPassword);
router.post('/FindUser',UserController.getFindUser);

router.post('/logout',middleware,UserController.CerrarSesion);
router.get('/getCookie',middleware, UserController.VerificarSesion);
export default router;