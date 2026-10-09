package com.donations.donations.application.usecase.creator;
import com.donations.donations.application.port.out.CreatorPort;
import com.donations.donations.domain.model.Creator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RequestFullNameChangeUseCase {
    private final CreatorPort creatorPort;
    public Creator execute(UUID userId, String requestedFullName) {
        // Mock
        Creator creator = creatorPort.findByUserId(userId).orElseThrow();
        creator.setRequestedFullName(requestedFullName);
        return creatorPort.save(creator);
    }
}
