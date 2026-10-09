package com.donations.donations.repository;

import com.donations.donations.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SpringDataAuditLogRepository extends JpaRepository<AuditLog, UUID> {
}
