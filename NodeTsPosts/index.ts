import express from 'express'
import multer from 'multer'
import path from 'path'
import fs from 'fs'

// Configuración por entorno (ver .env.example)
const PORT = Number(process.env.PORT ?? 3001);
const UPLOAD_DIR = path.resolve(process.env.UPLOAD_DIR ?? 'upload');

fs.mkdirSync(UPLOAD_DIR, { recursive: true });

const app = express();

const storage = multer.diskStorage({
    destination: (req, file, cb) => {
        cb(null, UPLOAD_DIR)
    },
    filename: (req, file, cb) => {
        const uniqueSuffix = Date.now() + "-" + Math.round(Math.random() * 1E9);
        const fileExtension = path.extname(file.originalname)
        cb(null, file.fieldname + "-" + uniqueSuffix + fileExtension)
    },
});

const upload = multer({ storage })

app.get('/health', (req, res) => {
    res.send({ status: 'UP' });
});

app.post('/subirImagen', upload.single('miArchivo'), (req, res) => {
    console.log(req.file);
    res.send({
        messsage: 'Archivo subido exitosamente',
        data: req.file
    });
});

app.use('/uploads', express.static(UPLOAD_DIR));

app.listen(PORT, () => {
    console.log(`media service on http://localhost:${PORT} (uploads: ${UPLOAD_DIR})`);
})
