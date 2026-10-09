package com.donations.donations.application.usecase.admin;

import com.donations.donations.domain.model.Report;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetReportsUseCase {
    public List<Report> execute() {
        return List.of();
    }
}
