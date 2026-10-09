package com.donations.donations.service.creator;

import com.donations.donations.repository.CreatorRepository;
import com.donations.donations.repository.VerificationRequestRepository;
import com.donations.donations.model.Creator;
import com.donations.donations.model.CreatorStatus;
import com.donations.donations.model.VerificationRequest;
import com.donations.donations.model.enums.VerificationRequestStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubmitVerificationUseCase {
    private final VerificationRequestRepository verificationRepository;
    private final CreatorRepository creatorRepository;

    public VerificationRequest execute(UUID creatorId, VerificationRequest request) {
        Creator creator = creatorRepository.findById(creatorId)
                .orElseThrow(() -> new IllegalArgumentException("Creator not found"));
                
        // creator status remains as is until approved
        creator.setRequestedFullName(request.getFullName());
        creator.setSubmittedAt(LocalDateTime.now());
        creatorRepository.save(creator);

        request.setCreatorId(creatorId);
        request.setStatus(VerificationRequestStatus.PENDING);
        return verificationRepository.save(request);
    }
}