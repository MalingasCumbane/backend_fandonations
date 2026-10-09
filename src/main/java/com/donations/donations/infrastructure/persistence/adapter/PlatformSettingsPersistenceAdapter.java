package com.donations.donations.infrastructure.persistence.adapter;

import com.donations.donations.application.port.out.PlatformSettingsPort;
import com.donations.donations.domain.model.PlatformSettings;
import com.donations.donations.infrastructure.persistence.entity.PlatformSettingsJpaEntity;
import com.donations.donations.infrastructure.persistence.mapper.PlatformSettingsMapper;
import com.donations.donations.infrastructure.persistence.repository.PlatformSettingsJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PlatformSettingsPersistenceAdapter implements PlatformSettingsPort {

    private final PlatformSettingsJpaRepository repository;

    @Override
    public PlatformSettings save(PlatformSettings settings) {
        PlatformSettingsJpaEntity entity = PlatformSettingsMapper.toEntity(settings);
        PlatformSettingsJpaEntity savedEntity = repository.save(entity);
        return PlatformSettingsMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<PlatformSettings> getSettings() {
        return repository.findAll().stream().findFirst().map(PlatformSettingsMapper::toDomain);
    }

    @Override
    public Optional<PlatformSettings> findById(UUID id) {
        return repository.findById(id).map(PlatformSettingsMapper::toDomain);
    }
}
