package Proconnect.backProconnect.model;

import java.util.Date;

import Proconnect.backProconnect.Entity.PostulationStatus;

public record PostulationDto(Long id, Long userId, Long jobId, Date date, PostulationStatus status) {
}
