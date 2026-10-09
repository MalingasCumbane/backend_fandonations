package com.donations.donations.service.admin;

import com.donations.donations.repository.CreatorRepository;
import com.donations.donations.repository.DonationRepository;
import com.donations.donations.repository.PlatformSettingsRepository;
import com.donations.donations.model.Creator;
import com.donations.donations.model.CreatorStatus;
import com.donations.donations.model.Donation;
import com.donations.donations.model.PlatformSettings;
//import com.donations.donations.model.enums.DonationStatus;
import com.donations.donations.model.enums.DonationStatus;
import com.donations.donations.dto.admin.AdminStatsResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetAdminStatsUseCase {
    private final CreatorRepository creatorRepository;
    private final DonationRepository donationRepository;
    private final PlatformSettingsRepository settingsRepository;

    public AdminStatsResponseDto execute() {
        List<Creator> creators = creatorRepository.findAll();
        List<Donation> donations = donationRepository.findAll();
        
        long totalCreators = creators.size();
        long verifiedCreators = creators.stream().filter(c -> c.getStatus() == CreatorStatus.VERIFIED).count();
        long pendingCreators = creators.stream().filter(c -> c.getStatus() == CreatorStatus.PENDING_VERIFICATION).count();

        long successDonations = donations.stream().filter(d -> d.getStatus() == DonationStatus.SUCCESS).count();
        long failedDonations = donations.stream().filter(d -> d.getStatus() == DonationStatus.FAILED).count();

        BigDecimal volume = donations.stream()
                .filter(d -> d.getStatus() == DonationStatus.SUCCESS)
                .map(Donation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal revenue = donations.stream()
                .filter(d -> d.getStatus() == DonationStatus.SUCCESS)
                .map(Donation::getFeeAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
        PlatformSettings settings = settingsRepository.getSettings().orElse(null);
        BigDecimal fee = settings != null ? settings.getPlatformFeePercentage() : BigDecimal.TEN;

        return AdminStatsResponseDto.builder()
                .stats(AdminStatsResponseDto.PlatformStats.builder()
                        .creators((int)totalCreators)
                        .verified((int)verifiedCreators)
                        .pending((int)pendingCreators)
                        .donations((int)successDonations)
                        .volume(volume)
                        .revenue(revenue)
                        .failed((int)failedDonations)
                        .feePercentage(fee)
                        .build())
                .perDay(List.of())
                .perMonth(List.of())
                .byMethod(List.of())
                .build();
    }
}