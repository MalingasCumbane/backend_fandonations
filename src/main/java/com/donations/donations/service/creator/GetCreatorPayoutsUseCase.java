package com.donations.donations.service.creator;

import com.donations.donations.repository.PayoutRepository;
import com.donations.donations.model.Payout;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.donations.donations.dto.PageResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetCreatorPayoutsUseCase {
    private final PayoutRepository payoutRepository;

    public List<Payout> execute(UUID creatorId) {
        return payoutRepository.findByCreatorId(creatorId);
    }

    public PageResponse<Payout> executePaged(UUID creatorId, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
        return PageResponse.of(payoutRepository.findByCreatorIdOrderByCreatedAtDesc(creatorId, pageable));
    }
}
