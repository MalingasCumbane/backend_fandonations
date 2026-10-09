package com.donations.donations.infrastructure.persistence.repository;

import com.donations.donations.infrastructure.persistence.entity.ReportJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReportJpaRepository extends JpaRepository<ReportJpaEntity, UUID> {
}
