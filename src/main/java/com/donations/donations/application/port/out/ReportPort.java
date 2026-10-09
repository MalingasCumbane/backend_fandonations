package com.donations.donations.application.port.out;

import com.donations.donations.domain.model.Report;
import java.util.Optional;
import java.util.UUID;

public interface ReportPort {
    Report save(Report report);
    Optional<Report> findById(UUID id);
}
