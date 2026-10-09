package com.donations.donations.application.usecase.creator;

import com.donations.donations.application.port.out.VerificationRequestPort;
import com.donations.donations.domain.model.VerificationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetCreatorVerificationUseCase {
    private final VerificationRequestPort verificationPort;

    public Optional<VerificationRequest> execute(UUID creatorId) {
        return verificationPort.findByCreatorId(creatorId);
    }
}