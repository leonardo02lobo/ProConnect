import pool from "./database";
import { EmpresaModel, OrganizarDatosEmpresa, type Empresas } from "./Empresas";

export interface Empleo {
    id: number;
    idEmpresa: number;
    empresa: Empresas;
    puesto: string;
    descripcion: string;
    Titulo: string;
    fecha: Date;
}

export const empleosModel = {
    async createEmpleo(empleo: Empleo){
        const [result] = await pool.query("INSERT INTO proconnect.empleos (id_empresa, puesto, descripcion, Titulo) VALUES (?, ?, ?, ?)", [empleo.idEmpresa, empleo.puesto, empleo.descripcion, empleo.Titulo])
        return result
    },
    async getAll(){
        const [row] = await pool.query("SELECT * FROM proconnect.empleos")
        return row
    },
    async getByID(id: string){
        const [row] = await pool.query("SELECT * FROM proconnect.empleos WHERE id = ?", [id])
        return row
    },
    async getByPuesto(puesto: string){
        const [row] = await pool.query("SELECT * FROM proconnect.empleos WHERE puesto = ?", [puesto])
        return row
    },
    async BuscarEmpleoPorNombre(nombre: string){
        const nombreConWildcards = `%${nombre}%`
        const [row] = await pool.query("select * from proconnect.empleos where Titulo like ? || descripcion like ? || puesto like ?;"
        , [nombreConWildcards, nombreConWildcards, nombreConWildcards])
        return row
    }
}

export async function OrganizarDatosEmpleo(empleo: any): Promise<Empleo>{
    const empresamodel:any = await EmpresaModel.ObtenerEmpresaByID(empleo['id_empresa'])
    const empresa: Empresas = await OrganizarDatosEmpresa(empresamodel[0])
    return {
        id: empleo.id,
        idEmpresa: empleo.idEmpresa,
        empresa: empresa,
        puesto: empleo.puesto,
        descripcion: empleo.descripcion,
        Titulo: empleo.Titulo,
        fecha: empleo.fecha
    };
}