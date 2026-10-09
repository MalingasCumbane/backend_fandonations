package com.donations.donations.application.port.out;

import com.donations.donations.domain.model.Payout;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PayoutPort {
    Payout save(Payout payout);
    Optional<Payout> findById(UUID id);
    List<Payout> findByCreatorId(UUID creatorId);
}
