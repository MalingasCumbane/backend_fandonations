package com.donations.donations.service.creator;

import com.donations.donations.repository.PayoutRepository;
import com.donations.donations.model.Payout;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetCreatorPayoutsUseCase {
    private final PayoutRepository payoutRepository;

    public List<Payout> execute(UUID creatorId) {
        return payoutRepository.findByCreatorId(creatorId);
    }
}
