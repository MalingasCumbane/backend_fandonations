package com.donations.donations.service.creator;

import com.donations.donations.repository.VerificationRequestRepository;
import com.donations.donations.model.VerificationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetCreatorVerificationUseCase {
    private final VerificationRequestRepository verificationRepository;

    public Optional<VerificationRequest> execute(UUID creatorId) {
        return verificationRepository.findFirstByCreatorIdOrderByCreatedAtDesc(creatorId);
    }
}