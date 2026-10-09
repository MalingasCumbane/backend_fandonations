package com.donations.donations.infrastructure.persistence.adapter;

import com.donations.donations.application.port.out.ReportPort;
import com.donations.donations.domain.model.Report;
import com.donations.donations.infrastructure.persistence.entity.ReportJpaEntity;
import com.donations.donations.infrastructure.persistence.mapper.ReportMapper;
import com.donations.donations.infrastructure.persistence.repository.ReportJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReportPersistenceAdapter implements ReportPort {

    private final ReportJpaRepository reportRepository;

    @Override
    public Report save(Report report) {
        ReportJpaEntity entity = ReportMapper.toEntity(report);
        ReportJpaEntity savedEntity = reportRepository.save(entity);
        return ReportMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Report> findById(UUID id) {
        return reportRepository.findById(id)
                .map(ReportMapper::toDomain);
    }
}
