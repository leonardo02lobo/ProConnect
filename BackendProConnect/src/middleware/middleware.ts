import { Request,Response,NextFunction } from "express";
import jwt from "jsonwebtoken";

export const middleware = (req: Request, res: Response, next: NextFunction): void => {
    try {
        const token = req.cookies.authToken || req.headers.authorization?.split(' ')[1];
        if(!token){
            res.status(401).json({error: "Acceso no autorizado"});
            return;
        }
        const decoded = jwt.verify(token, process.env.JWT_SECRET || "OVoBh9p438F2UCHZwjMEvsbRG0etDikA");
        
        (req as any).user = decoded;
        
        next();
    } catch(e) {
        res.status(401).json({ error: (e as Error).message }); 
        return;
    }
}