package com.donations.donations.application.usecase.creator;

import com.donations.donations.application.port.out.PayoutPort;
import com.donations.donations.domain.model.Payout;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetCreatorPayoutsUseCase {
    private final PayoutPort payoutPort;

    public List<Payout> execute(UUID creatorId) {
        return payoutPort.findByCreatorId(creatorId);
    }
}
