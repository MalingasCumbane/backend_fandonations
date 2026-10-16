package com.donations.donations.service.admin;

import com.donations.donations.repository.CreatorRepository;
import com.donations.donations.repository.DonationRepository;
import com.donations.donations.repository.PlatformSettingsRepository;
import com.donations.donations.model.Creator;
import com.donations.donations.model.CreatorStatus;
import com.donations.donations.model.Donation;
import com.donations.donations.model.PlatformSettings;
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
    private final com.donations.donations.repository.PayoutRepository payoutRepository;
    private final PlatformSettingsRepository settingsRepository;

    public AdminStatsResponseDto execute() {
        List<Creator> creators = creatorRepository.findAll();
        List<Donation> donations = donationRepository.findAll();
        List<com.donations.donations.model.Payout> payouts = payoutRepository.findAll();
        
        long totalCreators = creators.size();
        long verifiedCreators = creators.stream().filter(c -> c.getStatus() == CreatorStatus.VERIFIED).count();
        long pendingCreators = creators.stream().filter(c -> c.getStatus() == CreatorStatus.PENDING_VERIFICATION).count();

        long successDonations = donations.stream().filter(d -> d.getStatus() == DonationStatus.SUCCESS).count();
        long failedDonations = donations.stream().filter(d -> d.getStatus() == DonationStatus.FAILED).count();

        BigDecimal volume = donations.stream()
                .filter(d -> d.getStatus() == DonationStatus.SUCCESS)
                .map(Donation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        PlatformSettings settings = settingsRepository.getSettings().orElse(null);
        BigDecimal fee = (settings != null && settings.getPlatformFeePercentage() != null) 
                ? settings.getPlatformFeePercentage() 
                : BigDecimal.TEN;

        // Receita das taxas da plataforma:
        // 1. Taxas explicitamente cobradas em doações (caso existam)
        BigDecimal donationFees = donations.stream()
                .filter(d -> d.getStatus() == DonationStatus.SUCCESS)
                .map(Donation::getFeeAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. Taxas cobradas sobre levantamentos executados (PAID) com base na taxa em vigor
        BigDecimal payoutFees = payouts.stream()
                .filter(p -> p.getStatus() == com.donations.donations.model.enums.PayoutStatus.PAID)
                .map(p -> p.getAmount().multiply(fee).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal revenue = donationFees.add(payoutFees);
        // Se a plataforma calcula a receita diretamente sobre o volume total transacionado conforme a taxa em vigor:
        if (revenue.compareTo(BigDecimal.ZERO) == 0 && volume.compareTo(BigDecimal.ZERO) > 0) {
            revenue = volume.multiply(fee).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
        }

        // Ultimos 14 dias (perDay)
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.util.Map<String, BigDecimal> dayBuckets = new java.util.LinkedHashMap<>();
        for (int i = 13; i >= 0; i--) {
            java.time.LocalDateTime day = now.minusDays(i);
            String label = day.format(java.time.format.DateTimeFormatter.ofPattern("d/M"));
            dayBuckets.put(label, BigDecimal.ZERO);
        }
        for (Donation d : donations) {
            if (d.getStatus() == DonationStatus.SUCCESS && d.getCreatedAt() != null) {
                String label = d.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("d/M"));
                if (dayBuckets.containsKey(label)) {
                    dayBuckets.put(label, dayBuckets.get(label).add(d.getAmount()));
                }
            }
        }
        List<AdminStatsResponseDto.SeriesPoint> perDay = dayBuckets.entrySet().stream()
                .map(e -> AdminStatsResponseDto.SeriesPoint.builder().label(e.getKey()).value(e.getValue()).build())
                .collect(java.util.stream.Collectors.toList());

        // Ultimos 6 meses (perMonth)
        String[] ptMonths = {"Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez"};
        java.util.Map<String, BigDecimal> monthBuckets = new java.util.LinkedHashMap<>();
        for (int i = 5; i >= 0; i--) {
            java.time.LocalDateTime m = now.minusMonths(i);
            String label = ptMonths[m.getMonthValue() - 1];
            monthBuckets.put(label, BigDecimal.ZERO);
        }
        for (Donation d : donations) {
            if (d.getStatus() == DonationStatus.SUCCESS && d.getCreatedAt() != null) {
                int monthIdx = d.getCreatedAt().getMonthValue() - 1;
                String label = ptMonths[monthIdx];
                if (monthBuckets.containsKey(label)) {
                    monthBuckets.put(label, monthBuckets.get(label).add(d.getAmount()));
                }
            }
        }
        List<AdminStatsResponseDto.SeriesPoint> perMonth = monthBuckets.entrySet().stream()
                .map(e -> AdminStatsResponseDto.SeriesPoint.builder().label(e.getKey()).value(e.getValue()).build())
                .collect(java.util.stream.Collectors.toList());

        // Volume por metodo (byMethod)
        java.util.Map<com.donations.donations.model.enums.PaymentMethod, BigDecimal> methodSums = donations.stream()
                .filter(d -> d.getStatus() == DonationStatus.SUCCESS && d.getMethod() != null)
                .collect(java.util.stream.Collectors.groupingBy(Donation::getMethod, java.util.stream.Collectors.reducing(BigDecimal.ZERO, Donation::getAmount, BigDecimal::add)));

        BigDecimal sumAllMethods = methodSums.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        List<AdminStatsResponseDto.MethodShare> byMethod = java.util.Arrays.stream(com.donations.donations.model.enums.PaymentMethod.values()).map(method -> {
            BigDecimal amt = methodSums.getOrDefault(method, BigDecimal.ZERO);
            BigDecimal share = sumAllMethods.compareTo(BigDecimal.ZERO) > 0 
                    ? amt.multiply(new BigDecimal("100")).divide(sumAllMethods, 2, java.math.RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            return AdminStatsResponseDto.MethodShare.builder()
                    .method(method.name())
                    .value(amt)
                    .share(share)
                    .build();
        }).collect(java.util.stream.Collectors.toList());

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
                .perDay(perDay)
                .perMonth(perMonth)
                .byMethod(byMethod)
                .build();
    }
}