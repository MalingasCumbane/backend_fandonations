package com.donations.donations.service.publics;

import com.donations.donations.repository.DonationRepository;
import com.donations.donations.model.Donation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetDonationStatusUseCase {
    private final DonationRepository donationRepository;

    public Optional<Donation> execute(String reference) {
        return donationRepository.findByReference(reference);
    }
}
