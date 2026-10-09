with open('src/main/java/com/donations/donations/service/admin/GetVerificationsUseCase.java', 'r') as f:
    content = f.read()

replacement = """package com.donations.donations.service.admin;

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
"""

with open('src/main/java/com/donations/donations/service/admin/GetVerificationsUseCase.java', 'w') as f:
    f.write(replacement)
