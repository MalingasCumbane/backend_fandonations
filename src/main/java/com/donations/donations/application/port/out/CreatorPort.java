package com.donations.donations.application.port.out;

import com.donations.donations.domain.model.Creator;
import java.util.Optional;
import java.util.UUID;

public interface CreatorPort {
    Optional<Creator> findByUserId(UUID userId);
    Optional<Creator> findByUsername(String username);
    Creator save(Creator creator);
}
