import express from 'express'
import multer from 'multer'
import path from 'path'

const app = express();

const storage = multer.diskStorage({
    destination: (req,file,cb) => {
        cb(null,'upload/')
    },
    filename: (req,file,cb) => {
        const uniqueSuffix = Date.now() + "-" + Math.round(Math.random() * 1E9);
        const fileExtension = path.extname(file.originalname)
        cb(null, file.fieldname+"-"+uniqueSuffix+fileExtension)
    },
});

const upload = multer({storage})


app.post('/subirImagen', upload.single('miArchivo'), (req, res) => {
  console.log(req.file);
  res.send('Archivo subido exitosamente');
});

app.use('/uploads', express.static('upload'));

app.listen(3000, () => {
    console.log("http://localhost:3000");
})