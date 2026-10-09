package com.donations.donations.application.usecase.admin;

import com.donations.donations.application.port.out.PlatformSettingsPort;
import com.donations.donations.domain.model.PlatformSettings;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("adminGetPlatformSettingsUseCase")
@RequiredArgsConstructor
public class GetPlatformSettingsUseCase {
    private final PlatformSettingsPort settingsPort;

    public PlatformSettings execute() {
        // Assume ID is fixed or just return first
        return settingsPort.getSettings()
                .orElse(null);
    }
}
