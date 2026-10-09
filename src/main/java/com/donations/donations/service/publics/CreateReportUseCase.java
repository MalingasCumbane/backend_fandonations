package com.donations.donations.service.publics;

import com.donations.donations.repository.ReportRepository;
import com.donations.donations.model.Report;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CreateReportUseCase {
    private final ReportRepository reportRepository;

    public Report execute(Report report) {
        report.setCreatedAt(LocalDateTime.now());
        return reportRepository.save(report);
    }
}
