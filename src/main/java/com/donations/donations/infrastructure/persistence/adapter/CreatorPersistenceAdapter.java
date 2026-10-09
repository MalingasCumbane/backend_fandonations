package com.donations.donations.infrastructure.persistence.adapter;

import com.donations.donations.application.port.out.CreatorPort;
import com.donations.donations.domain.model.Creator;
import com.donations.donations.infrastructure.persistence.entity.CreatorJpaEntity;
import com.donations.donations.infrastructure.persistence.mapper.CreatorMapper;
import com.donations.donations.infrastructure.persistence.repository.CreatorJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CreatorPersistenceAdapter implements CreatorPort {

    private final CreatorJpaRepository repository;
    private final CreatorMapper mapper;

    @Override
    public Optional<Creator> findByUserId(UUID userId) {
        return repository.findByUserId(userId).map(mapper::toDomain);
    }

    @Override
    public Optional<Creator> findByUsername(String username) {
        return repository.findByUsername(username).map(mapper::toDomain);
    }

    @Override
    public Optional<Creator> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Creator> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public Creator save(Creator creator) {
        CreatorJpaEntity entity = mapper.toEntity(creator);

        // If it's an update, preserve base fields
        if (creator.getId() != null) {
            CreatorJpaEntity existing = repository.findById(creator.getId()).orElseThrow();
            entity.setCreatedAt(existing.getCreatedAt());
            entity.setCreatedBy(existing.getCreatedBy());
            entity.setCreatedIp(existing.getCreatedIp());
            entity.setReference(existing.getReference());
            entity.setIsActive(existing.getIsActive());
            entity.setIsDeleted(existing.getIsDeleted());
        }

        CreatorJpaEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }
}
