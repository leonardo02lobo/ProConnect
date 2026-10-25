package Proconnect.backProconnect.model;

import java.time.Instant;

public record JobDto(
        Long id,
        Long companyId,
        String title,
        String description,
        String workStation,
        Instant date) {}
