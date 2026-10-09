package com.donations.donations.application.usecase.publics;

import com.donations.donations.application.port.out.ReportPort;
import com.donations.donations.domain.model.Report;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CreateReportUseCase {
    private final ReportPort reportPort;

    public Report execute(Report report) {
        report.setCreatedAt(LocalDateTime.now());
        return reportPort.save(report);
    }
}
