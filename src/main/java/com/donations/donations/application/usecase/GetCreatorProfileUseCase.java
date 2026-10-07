package com.donations.donations.application.usecase;

import com.donations.donations.application.port.out.CreatorPort;
import com.donations.donations.domain.model.Creator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetCreatorProfileUseCase {

    private final CreatorPort creatorPort;

    public Optional<Creator> execute(UUID userId) {
        return creatorPort.findByUserId(userId);
    }
}
