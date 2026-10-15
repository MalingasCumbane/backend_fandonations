package com.donations.donations.service.admin;

import com.donations.donations.model.Payout;
import com.donations.donations.repository.PayoutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetPayoutsUseCase {
    private final PayoutRepository repository;

    public List<Payout> execute() {
        return repository.findAll();
    }

    public com.donations.donations.dto.PageResponse<Payout> executePaged(int page, int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(Math.max(0, page), Math.max(1, size));
        return com.donations.donations.dto.PageResponse.of(repository.findAllByOrderByCreatedAtDesc(pageable));
    }
}
