package com.donations.donations.service.creator;

import com.donations.donations.repository.CampaignRepository;
import com.donations.donations.model.Campaign;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.donations.donations.dto.PageResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetCreatorCampaignsUseCase {
    private final CampaignRepository campaignRepository;

    public List<Campaign> execute(UUID creatorId) {
        return campaignRepository.findByCreatorId(creatorId);
    }

    public PageResponse<Campaign> executePaged(UUID creatorId, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
        return PageResponse.of(campaignRepository.findByCreatorIdOrderByCreatedAtDesc(creatorId, pageable));
    }
}
