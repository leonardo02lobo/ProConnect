package Proconnect.backProconnect.model;

import Proconnect.backProconnect.Entity.PostulationStatus;
import java.util.Date;

public record PostulationDto(
        Long id, Long userId, Long jobId, Date date, PostulationStatus status) {}
