package com.donations.donations.service;

import com.donations.donations.repository.CreatorRepository;
import com.donations.donations.model.Creator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetCreatorProfileUseCase {

    private final CreatorRepository creatorRepository;

    public Optional<Creator> execute(UUID userId) {
        return creatorRepository.findByUserId(userId);
    }
}
