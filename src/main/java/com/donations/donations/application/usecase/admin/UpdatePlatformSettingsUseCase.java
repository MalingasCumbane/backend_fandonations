package com.donations.donations.application.usecase.admin;

import com.donations.donations.application.port.out.PlatformSettingsPort;
import com.donations.donations.domain.model.PlatformSettings;
import com.donations.donations.presentation.rest.dto.admin.SettingsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdatePlatformSettingsUseCase {
    private final PlatformSettingsPort settingsPort;

    public PlatformSettings execute(SettingsDto dto) {
        PlatformSettings settings = settingsPort.getSettings().orElseThrow();

        if (dto.getPlatformFeePercentage() != null) settings.setPlatformFeePercentage(dto.getPlatformFeePercentage());
        if (dto.getMinPayoutAmount() != null) settings.setMinPayoutAmount(dto.getMinPayoutAmount());
        if (dto.getPayoutsEnabled() != null) settings.setPayoutsEnabled(dto.getPayoutsEnabled());
        if (dto.getRegistrationsOpen() != null) settings.setRegistrationsOpen(dto.getRegistrationsOpen());

        return settingsPort.save(settings);
    }
}
