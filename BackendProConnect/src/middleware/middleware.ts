import { Request,Response,NextFunction } from "express";
import jwt from "jsonwebtoken";

export const middleware = (req: Request, res: Response, next: NextFunction) => {
    try{
        const token = req.cookies.authToken || req.headers.authorization?.split(' ')[1];

        if(!token){
            res.status(401).json({error: "Acceso no autorizado"})
        }
        const decoded = jwt.verify(token,process.env.JWT_SECRET || "OVoBh9p438F2UCHZwjMEvsbRG0etDikA");
        
        next();
    }catch(e){
        res.status(401).json({ error: 'Token inválido' });
    }
}