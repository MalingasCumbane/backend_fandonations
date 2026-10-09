package com.donations.donations.service.admin;

import com.donations.donations.repository.PlatformSettingsRepository;
import com.donations.donations.model.PlatformSettings;
import com.donations.donations.dto.admin.SettingsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdatePlatformSettingsUseCase {
    private final PlatformSettingsRepository settingsRepository;

    public PlatformSettings execute(SettingsDto dto) {
        PlatformSettings settings = settingsRepository.getSettings().orElseThrow();

        if (dto.getPlatformFeePercentage() != null) settings.setPlatformFeePercentage(dto.getPlatformFeePercentage());
        if (dto.getMinPayoutAmount() != null) settings.setMinPayoutAmount(dto.getMinPayoutAmount());
        if (dto.getPayoutsEnabled() != null) settings.setPayoutsEnabled(dto.getPayoutsEnabled());
        if (dto.getRegistrationsOpen() != null) settings.setRegistrationsOpen(dto.getRegistrationsOpen());

        return settingsRepository.save(settings);
    }
}
