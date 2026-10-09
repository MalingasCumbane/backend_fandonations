package com.donations.donations.service.admin;

import com.donations.donations.repository.ReportRepository;
import com.donations.donations.model.Report;
import com.donations.donations.model.enums.ReportStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ReviewReportUseCase {
    private final ReportRepository reportRepository;

    public Report execute(UUID id, ReportStatus status) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found"));

        report.setStatus(status);
        return reportRepository.save(report);
    }
}
