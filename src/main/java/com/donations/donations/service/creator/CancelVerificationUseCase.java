package com.donations.donations.service.creator;

import com.donations.donations.repository.CreatorRepository;
import com.donations.donations.repository.VerificationRequestRepository;
import com.donations.donations.model.Creator;
import com.donations.donations.model.enums.VerificationRequestStatus;
import com.donations.donations.model.CreatorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CancelVerificationUseCase {
    private final VerificationRequestRepository verificationRepository;
    private final CreatorRepository creatorRepository;

    public void execute(UUID creatorId) {
        Creator creator = creatorRepository.findById(creatorId)
                .orElseThrow(() -> new IllegalArgumentException("Creator not found"));
        
        verificationRepository.findFirstByCreatorIdOrderByCreatedAtDesc(creatorId)
            .ifPresent(req -> {
                if (req.getStatus() == VerificationRequestStatus.PENDING_REQUEST) {
                    verificationRepository.delete(req);
                    if (creator.getStatus() == CreatorStatus.PENDING_VERIFICATION) {
                        creator.setStatus(CreatorStatus.DRAFT);
                        creatorRepository.save(creator);
                    }
                } else {
                    throw new IllegalStateException("Apenas pedidos pendentes podem ser cancelados.");
                }
            });
    }
}
