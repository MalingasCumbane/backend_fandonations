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

    public com.donations.donations.dto.PageResponse<Donation> executePaged(int page, int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(Math.max(0, page), Math.max(1, size));
        return com.donations.donations.dto.PageResponse.of(repository.findAllByOrderByCreatedAtDesc(pageable));
    }
}
