package com.donations.donations.application.port.out;

import com.donations.donations.domain.model.Creator;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CreatorPort {
    Optional<Creator> findByUserId(UUID userId);
    Optional<Creator> findByUsername(String username);
    Creator save(Creator creator);
    List<Creator> findAll();
    Optional<Creator> findById(UUID id);
}
