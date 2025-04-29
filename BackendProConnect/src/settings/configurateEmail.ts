import nodemailer from "nodemailer"

export const transporter = nodemailer.createTransport({
    host: "smtp.gmail.com",
    port: 465,
    secure: true,
    auth: {
        user: "loboleonardo75@gmail.com",
        pass: "vjpf myuf wzwu ewib",
    },
});

export function MensajePositivo(nombre: string, puesto: string,nombreEmpresa: string): string {
    return `
    ¡Felicidades! Has sido seleccionado para el puesto

    Hola ${nombre},

    ¡Nos da mucho gusto informarte que has sido seleccionado para el puesto de ${puesto} en ${nombreEmpresa}!

    Quedamos impresionados con tu perfil, tus habilidades y tu entusiasmo demostrada durante el proceso de selección. Estamos convencidos de que serás un gran aporte para nuestro equipo.

    En breve, nos pondremos en contacto contigo para darte más detalles sobre tu incorporación y los pasos a seguir.

    ¡Bienvenido a bordo!

    Atentamente,
    ${nombreEmpresa}
    `
}

export function MensajeNegativo(nombre: string, puesto: string,nombreEmpresa: string): string {
    return `
    Resultado de tu postulación al puesto de ${puesto}

    Hola ${nombre}

    Agradecemos sinceramente tu interés en formar parte de ${nombreEmpresa} y por el tiempo que dedicaste a nuestro proceso de selección para el puesto de ${puesto}.

    Luego de una cuidadosa evaluación, hemos decidido continuar con otros candidatos cuyos perfiles cuentan con una mayor alineación en términos de experiencia para este momento.

    Esto en ningún momento refleja una valoración negativa de tu potencial. Creemos que tienes mucho que ofrecer y te invitamos a seguir aplicando a nuevas oportunidades que puedan surgir en el futuro.

    ¡Mucha suerte en tu búsqueda laboral y gracias nuevamente por tu interés!

    Atentamente,
    ${nombreEmpresa}
    `
}