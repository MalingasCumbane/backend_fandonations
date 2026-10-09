package com.donations.donations.service.creator;
import com.donations.donations.repository.CreatorRepository;
import com.donations.donations.model.Creator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RequestFullNameChangeUseCase {
    private final CreatorRepository creatorRepository;
    public Creator execute(UUID userId, String requestedFullName) {
        // Mock
        Creator creator = creatorRepository.findByUserId(userId).orElseThrow();
        creator.setRequestedFullName(requestedFullName);
        return creatorRepository.save(creator);
    }
}
