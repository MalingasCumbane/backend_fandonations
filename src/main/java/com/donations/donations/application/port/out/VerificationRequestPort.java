package com.donations.donations.application.port.out;

import com.donations.donations.domain.model.VerificationRequest;
import java.util.Optional;
import java.util.UUID;

public interface VerificationRequestPort {
    VerificationRequest save(VerificationRequest verificationrequest);
    Optional<VerificationRequest> findById(UUID id);
    Optional<VerificationRequest> findByCreatorId(UUID creatorId);
}
