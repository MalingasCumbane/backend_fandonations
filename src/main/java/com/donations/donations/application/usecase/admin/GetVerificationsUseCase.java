package com.donations.donations.application.usecase.admin;

import com.donations.donations.domain.model.VerificationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetVerificationsUseCase {
    // Mocking for now, will fix port later if needed
    public List<VerificationRequest> execute() {
        return List.of();
    }
}
