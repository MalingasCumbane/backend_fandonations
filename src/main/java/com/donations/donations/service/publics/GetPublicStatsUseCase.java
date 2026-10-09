package com.donations.donations.service.publics;

import com.donations.donations.repository.CreatorRepository;
import com.donations.donations.repository.DonationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class GetPublicStatsUseCase {
    private final CreatorRepository creatorRepository;
    private final DonationRepository donationRepository;

    public Map<String, Object> execute() {
        return Map.of(
                "totalCreators", creatorRepository.findAll().size(),
                "totalDonations", 0
        );
    }
}
