import { transporter } from "../settings/configurateEmail";

export async function DataCorreo(puesto: string, message: string,correoDestino: string){
    const info = await transporter.sendMail({
        from: '"Respuesta a su solicitud de empleo" <loboleonardo75@gmail.com>',
        to: correoDestino,
        subject: `Respuesta hacia el puesto de: ${puesto}`, 
        text: message,
      });
}