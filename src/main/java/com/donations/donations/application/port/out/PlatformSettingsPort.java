package com.donations.donations.application.port.out;

import com.donations.donations.domain.model.PlatformSettings;
import java.util.Optional;
import java.util.UUID;

public interface PlatformSettingsPort {
    PlatformSettings save(PlatformSettings platformsettings);
    Optional<PlatformSettings> findById(UUID id);
    Optional<PlatformSettings> getSettings();
}
