package com.donations.donations.infrastructure.persistence.repository;

import com.donations.donations.infrastructure.persistence.entity.PlatformSettingsJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PlatformSettingsJpaRepository extends JpaRepository<PlatformSettingsJpaEntity, UUID> {
}
