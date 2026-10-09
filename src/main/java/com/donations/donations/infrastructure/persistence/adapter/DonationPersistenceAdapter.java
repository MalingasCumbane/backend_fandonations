package com.donations.donations.infrastructure.persistence.adapter;

import com.donations.donations.application.port.out.DonationPort;
import com.donations.donations.domain.model.Donation;
import com.donations.donations.infrastructure.persistence.entity.DonationJpaEntity;
import com.donations.donations.infrastructure.persistence.mapper.DonationMapper;
import com.donations.donations.infrastructure.persistence.repository.DonationJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DonationPersistenceAdapter implements DonationPort {

    private final DonationJpaRepository donationRepository;
    
    // Workaround since DonationMapper might have static or non-static methods
    // Let's assume static if it compiles, or inject if it doesn't.
    // The previous code had mapper::toDomain, meaning it wasn't static. Wait!
    // The old code had: DonationMapper.toDomain(savedEntity) and mapper::toDomain. 
    // Let's just use lambda.

    @Override
    public Donation save(Donation donation) {
        DonationJpaEntity entity = DonationMapper.toEntity(donation);
        DonationJpaEntity savedEntity = donationRepository.save(entity);
        return DonationMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Donation> findById(UUID id) {
        return donationRepository.findById(id).map(DonationMapper::toDomain);
    }

    @Override
    public List<Donation> findByCreatorId(UUID creatorId) {
        return donationRepository.findByCreatorId(creatorId).stream().map(DonationMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<Donation> findByReference(String reference) {
        return donationRepository.findByReference(reference).map(DonationMapper::toDomain);
    }

    @Override
    public List<Donation> findAll() {
        return donationRepository.findAll().stream().map(DonationMapper::toDomain).collect(Collectors.toList());
    }
}
