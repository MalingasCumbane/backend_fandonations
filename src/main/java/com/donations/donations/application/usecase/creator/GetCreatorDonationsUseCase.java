package com.donations.donations.application.usecase.creator;

import com.donations.donations.application.port.out.DonationPort;
import com.donations.donations.domain.model.Donation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetCreatorDonationsUseCase {
    private final DonationPort donationPort;

    public List<Donation> execute(UUID creatorId) {
        return donationPort.findByCreatorId(creatorId);
    }
}
