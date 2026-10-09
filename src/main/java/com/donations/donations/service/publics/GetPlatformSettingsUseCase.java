package com.donations.donations.service.publics;

import com.donations.donations.repository.PlatformSettingsRepository;
import com.donations.donations.model.PlatformSettings;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("publicGetPlatformSettingsUseCase")
@RequiredArgsConstructor
public class GetPlatformSettingsUseCase {
    private final PlatformSettingsRepository platformSettingsRepository;

    public PlatformSettings execute() {
        return platformSettingsRepository.getSettings().orElse(new PlatformSettings());
    }
}
