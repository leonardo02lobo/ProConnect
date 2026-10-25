package Proconnect.backProconnect.model;

import java.util.Date;

public record JobDto(
        Long id, Long companyId, String title, String description, String workStation, Date date) {}
