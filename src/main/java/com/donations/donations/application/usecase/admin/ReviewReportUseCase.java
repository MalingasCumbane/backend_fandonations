package com.donations.donations.application.usecase.admin;

import com.donations.donations.application.port.out.ReportPort;
import com.donations.donations.domain.model.Report;
import com.donations.donations.domain.model.enums.ReportStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ReviewReportUseCase {
    private final ReportPort reportPort;

    public Report execute(UUID id, ReportStatus status) {
        Report report = reportPort.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found"));

        report.setStatus(status);
        return reportPort.save(report);
    }
}
