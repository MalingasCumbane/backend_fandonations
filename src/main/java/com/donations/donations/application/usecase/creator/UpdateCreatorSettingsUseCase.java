package com.donations.donations.application.usecase.creator;
import com.donations.donations.application.port.out.CreatorPort;
import com.donations.donations.domain.model.Creator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateCreatorSettingsUseCase {
    private final CreatorPort creatorPort;
    public Creator execute(UUID userId, Object dto) {
        // Mock
        return creatorPort.findByUserId(userId).orElseThrow();
    }
}
