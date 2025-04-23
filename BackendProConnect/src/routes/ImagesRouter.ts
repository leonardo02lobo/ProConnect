import { Router } from "express";
import { imagesController } from "../controllers/ImagesController";
import { middleware } from "../middleware/middleware";

const router = Router();

router.post('/SubirImagen',middleware,imagesController.SubirImagenes)

export default router;