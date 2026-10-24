package Proconnect.backProconnect.mail;

import Proconnect.backProconnect.Entity.PostulationStatus;

/**
 * Una postulación se aceptó o rechazó. Lleva ya resueltos los datos del correo (tomados de la BD dentro de la
 * transacción), así el envío asíncrono no vuelve a consultar ni depende de datos del request.
 */
public record PostulationResolvedEvent(
        Long postulationId,
        PostulationStatus status,
        String applicantEmail,
        String applicantName,
        String jobTitle,
        String companyName) {
}
