package com.donations.donations.infrastructure.persistence.mapper;

import com.donations.donations.domain.model.Report;
import com.donations.donations.infrastructure.persistence.entity.ReportJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class ReportMapper {

    public static Report toDomain(ReportJpaEntity entity) {
        if (entity == null) return null;
        return Report.builder()
                .id(entity.getId())
                .creatorId(entity.getCreatorId())
                .reason(entity.getReason())
                .details(entity.getDetails())
                .reporterEmail(entity.getReporterEmail())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static ReportJpaEntity toEntity(Report domain) {
        if (domain == null) return null;
        ReportJpaEntity entity = ReportJpaEntity.builder()
                .id(domain.getId())
                .creatorId(domain.getCreatorId())
                .reason(domain.getReason())
                .details(domain.getDetails())
                .reporterEmail(domain.getReporterEmail())
                .status(domain.getStatus())
                .build();

        // Initialize Base fields if new
        if (domain.getId() == null) {
            entity.setIsActive(true);
            entity.setIsDeleted(false);
            entity.setCreatedBy("system");
            entity.setCreatedIp("127.0.0.1");
            entity.setReference(java.util.UUID.randomUUID().toString().substring(0, 8));
        }

        return entity;
    }
}
