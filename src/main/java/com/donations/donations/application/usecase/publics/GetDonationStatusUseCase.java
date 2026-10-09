package com.donations.donations.application.usecase.publics;

import com.donations.donations.application.port.out.DonationPort;
import com.donations.donations.domain.model.Donation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetDonationStatusUseCase {
    private final DonationPort donationPort;

    public Optional<Donation> execute(String reference) {
        return donationPort.findByReference(reference);
    }
}
