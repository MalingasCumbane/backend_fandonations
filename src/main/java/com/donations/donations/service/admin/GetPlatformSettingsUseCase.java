package com.donations.donations.service.admin;

import com.donations.donations.repository.PlatformSettingsRepository;
import com.donations.donations.model.PlatformSettings;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("adminGetPlatformSettingsUseCase")
@RequiredArgsConstructor
public class GetPlatformSettingsUseCase {
    private final PlatformSettingsRepository settingsRepository;

    public PlatformSettings execute() {
        // Assume ID is fixed or just return first
        return settingsRepository.getSettings()
                .orElse(null);
    }
}
