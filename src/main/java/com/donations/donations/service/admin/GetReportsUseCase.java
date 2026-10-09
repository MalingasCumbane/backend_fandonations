package com.donations.donations.service.admin;

import com.donations.donations.model.Report;
import com.donations.donations.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetReportsUseCase {
    private final ReportRepository repository;

    public List<Report> execute() {
        return repository.findAll();
    }
}
