package com.donations.donations.application.usecase.publics;

import com.donations.donations.application.port.out.PlatformSettingsPort;
import com.donations.donations.domain.model.PlatformSettings;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("publicGetPlatformSettingsUseCase")
@RequiredArgsConstructor
public class GetPlatformSettingsUseCase {
    private final PlatformSettingsPort platformSettingsPort;

    public PlatformSettings execute() {
        return platformSettingsPort.getSettings().orElse(new PlatformSettings());
    }
}
