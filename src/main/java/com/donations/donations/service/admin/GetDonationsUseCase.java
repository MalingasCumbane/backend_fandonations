package com.donations.donations.service.admin;

import com.donations.donations.model.Donation;
import com.donations.donations.repository.DonationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetDonationsUseCase {
    private final DonationRepository repository;

    public List<Donation> execute() {
        return repository.findAll();
    }
}
