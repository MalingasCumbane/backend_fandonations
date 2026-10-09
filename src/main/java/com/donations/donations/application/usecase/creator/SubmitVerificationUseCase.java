package com.donations.donations.application.usecase.creator;

import com.donations.donations.application.port.out.CreatorPort;
import com.donations.donations.application.port.out.VerificationRequestPort;
import com.donations.donations.domain.model.Creator;
import com.donations.donations.domain.model.CreatorStatus;
import com.donations.donations.domain.model.VerificationRequest;
import com.donations.donations.domain.model.enums.VerificationRequestStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubmitVerificationUseCase {
    private final VerificationRequestPort verificationPort;
    private final CreatorPort creatorPort;

    public VerificationRequest execute(UUID creatorId, VerificationRequest request) {
        Creator creator = creatorPort.findById(creatorId)
                .orElseThrow(() -> new IllegalArgumentException("Creator not found"));
                
        // creator status remains as is until approved
        creator.setRequestedFullName(request.getFullName());
        creator.setSubmittedAt(LocalDateTime.now());
        creatorPort.save(creator);

        request.setCreatorId(creatorId);
        request.setStatus(VerificationRequestStatus.PENDING);
        return verificationPort.save(request);
    }
}