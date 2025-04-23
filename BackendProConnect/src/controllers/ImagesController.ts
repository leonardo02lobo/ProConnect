import { Request } from "express";

export const imagesController = {
    async SubirImagenes(req: Request) {
        if (!req.file) {
            throw new Error('No se proporcionó ninguna imagen');
        }
        
        return { 
            imageUrl: `/uploads/${req.file.filename}`,
            originalName: req.file.originalname
        };
    }
}