package Proconnect.backProconnect.model;

import Proconnect.backProconnect.Entity.PostulationStatus;
import java.time.Instant;

public record PostulationDto(
        Long id, Long userId, Long jobId, Instant date, PostulationStatus status) {}
