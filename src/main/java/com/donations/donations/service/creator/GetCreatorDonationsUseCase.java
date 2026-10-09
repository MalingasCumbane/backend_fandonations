package com.donations.donations.service.creator;

import com.donations.donations.repository.DonationRepository;
import com.donations.donations.model.Donation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetCreatorDonationsUseCase {
    private final DonationRepository donationRepository;

    public List<Donation> execute(UUID creatorId) {
        return donationRepository.findByCreatorId(creatorId);
    }
}
