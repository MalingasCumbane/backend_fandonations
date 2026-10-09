package com.donations.donations.service.creator;
import com.donations.donations.repository.CreatorRepository;
import com.donations.donations.model.Creator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateCreatorSettingsUseCase {
    private final CreatorRepository creatorRepository;
    public Creator execute(UUID userId, Object dto) {
        // Mock
        return creatorRepository.findByUserId(userId).orElseThrow();
    }
}
