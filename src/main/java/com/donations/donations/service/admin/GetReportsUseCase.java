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

    public com.donations.donations.dto.PageResponse<Report> executePaged(int page, int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(Math.max(0, page), Math.max(1, size));
        return com.donations.donations.dto.PageResponse.of(repository.findAllByOrderByCreatedAtDesc(pageable));
    }
}
