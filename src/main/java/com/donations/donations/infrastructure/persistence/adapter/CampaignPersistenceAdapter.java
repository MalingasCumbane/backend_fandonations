package com.donations.donations.infrastructure.persistence.adapter;

import com.donations.donations.application.port.out.CampaignPort;
import com.donations.donations.domain.model.Campaign;
import com.donations.donations.infrastructure.persistence.entity.CampaignJpaEntity;
import com.donations.donations.infrastructure.persistence.mapper.CampaignMapper;
import com.donations.donations.infrastructure.persistence.repository.CampaignJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CampaignPersistenceAdapter implements CampaignPort {

    private final CampaignJpaRepository campaignRepository;

    @Override
    public Campaign save(Campaign campaign) {
        CampaignJpaEntity entity = CampaignMapper.toEntity(campaign);
        CampaignJpaEntity savedEntity = campaignRepository.save(entity);
        return CampaignMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Campaign> findById(UUID id) {
        return campaignRepository.findById(id).map(CampaignMapper::toDomain);
    }

    @Override
    public List<Campaign> findByCreatorId(UUID creatorId) {
        return campaignRepository.findByCreatorId(creatorId).stream().map(CampaignMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        campaignRepository.findById(id).ifPresent(entity -> {
            entity.softDelete();
            campaignRepository.save(entity);
        });
    }
}
