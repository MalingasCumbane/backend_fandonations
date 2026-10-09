package com.donations.donations.application.usecase.publics;

import com.donations.donations.application.port.out.CreatorPort;
import com.donations.donations.application.port.out.DonationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class GetPublicStatsUseCase {
    private final CreatorPort creatorPort;
    private final DonationPort donationPort;

    public Map<String, Object> execute() {
        return Map.of(
                "totalCreators", creatorPort.findAll().size(),
                "totalDonations", 0
        );
    }
}
