import express from "express";
import userRouters from "./routes/userRoutes";
import EmpresaRouter from "./routes/EmpresaRouter"
import PublicacionRouter from "./routes/PublicacionRouter"
import ComentarioRouter from "./routes/ComentariosRouter"
import cookieParser from 'cookie-parser'
import cors from 'cors'
import dotenv from 'dotenv'

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

app.use((err: Error, req: express.Request, res: express.Response, next: express.NextFunction) => {
    console.error(err.stack);
    res.status(500).json({ error: 'Error interno del servidor' });
});

app.listen(3000, () => {
    console.log('Servidor en http://localhost:3000');
});