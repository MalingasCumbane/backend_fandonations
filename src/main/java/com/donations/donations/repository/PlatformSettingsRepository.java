package com.donations.donations.repository;

import com.donations.donations.model.PlatformSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PlatformSettingsRepository extends JpaRepository<PlatformSettings, UUID> {
    default java.util.Optional<PlatformSettings> getSettings() { return findAll().stream().findFirst(); }

}
