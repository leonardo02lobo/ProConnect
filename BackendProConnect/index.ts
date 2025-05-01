import express from "express";
import userRouters from "./src/routes/userRoutes";
import EmpresaRouter from "./src/routes/EmpresaRouter"
import PublicacionRouter from "./src/routes/PublicacionRouter"
import ComentarioRouter from "./src/routes/ComentariosRouter"
import AmigosRouter from "./src/routes/AmigosRouter"
import EmpleoRouter from "./src/routes/EmpleoRouter"
import postulacionRouter from "./src/routes/PostulacionesRouter"
import CorreosRouter from "./src/routes/CorreosRouter"
import cookieParser from 'cookie-parser'
import cors from 'cors'
import dotenv from 'dotenv'
import { upload } from "./src/settings/configurateImages";

dotenv.config()

const app = express();

app.use(cors({
    origin: process.env.FRONTEND_URL || 'http://localhost:4321',
    credentials: true
}))

app.use(express.json());
app.use(cookieParser());

app.use('/api/usuario', userRouters);
app.use('/api/Empresas', EmpresaRouter);
app.use('/api/Publicacion', PublicacionRouter);
app.use('/api/Comentarios',ComentarioRouter);
app.use('/api/Amigos',AmigosRouter);
app.use('/api/Empleo',EmpleoRouter);
app.use('/api/Postulaciones',postulacionRouter)
app.use('/api/Correos', CorreosRouter)

// Ruta para subir imágenes
app.post('/api/upload', upload.single('image'), (req, res, next) => {
    console.log('Después de Multer - File:', req.file);
    try {
        if (!req.file) {
            res.status(400).json({ error: 'No se proporcionó ninguna imagen' });
            return;
        }
        
        // Devuelve la URL donde se puede acceder a la imagen
        res.json({ 
            imageUrl: `/uploads/${req.file.filename}`,
            originalName: req.file.originalname
        });
    } catch (error) {
        console.log((error as Error).message)
        next(error);
    }
});

app.use('/uploads',express.static('uploads'));

app.use((err: Error, req: express.Request, res: express.Response, next: express.NextFunction) => {
    console.error(err.stack);
    res.status(500).json({ error: 'Error interno del servidor' });
});

app.listen(3000, () => {
    console.log('Servidor en http://localhost:3000');
});