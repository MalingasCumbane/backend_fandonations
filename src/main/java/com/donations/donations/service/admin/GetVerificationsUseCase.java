package com.donations.donations.service.admin;

import com.donations.donations.repository.VerificationRequestRepository;
import com.donations.donations.model.VerificationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetVerificationsUseCase {
    private final VerificationRequestRepository verificationRequestRepository;

    public List<VerificationRequest> execute() {
        return verificationRequestRepository.findAll();
    }
}
