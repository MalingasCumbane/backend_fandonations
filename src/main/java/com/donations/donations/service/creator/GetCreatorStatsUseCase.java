package com.donations.donations.service.creator;

import com.donations.donations.repository.DonationRepository;
import com.donations.donations.repository.PayoutRepository;
import com.donations.donations.model.Donation;
import com.donations.donations.model.Payout;
import com.donations.donations.model.enums.DonationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetCreatorStatsUseCase {
    private final DonationRepository donationRepository;
    private final PayoutRepository payoutRepository;
    private final com.donations.donations.repository.PlatformSettingsRepository settingsRepository;

    private static final String[] PT_MONTHS = {"Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez"};

    public Map<String, Object> execute(UUID creatorId) {
        List<Donation> donations = donationRepository.findByCreatorId(creatorId);
        List<Payout> payouts = payoutRepository.findByCreatorId(creatorId);

        BigDecimal totalSuccess = donations.stream().filter(d -> d.getStatus() == DonationStatus.SUCCESS).map(Donation::getAmount) // Gross amount
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalNet = donations.stream().filter(d -> d.getStatus() == DonationStatus.SUCCESS).map(Donation::getNetAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal feePercentage = settingsRepository.getSettings().map(s -> s.getPlatformFeePercentage() != null ? s.getPlatformFeePercentage() : new BigDecimal("10.00")).orElse(new BigDecimal("10.00"));

        BigDecimal donationFees = donations.stream().filter(d -> d.getStatus() == DonationStatus.SUCCESS).map(Donation::getFeeAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal payoutFees = payouts.stream().filter(p -> p.getStatus() == com.donations.donations.model.enums.PayoutStatus.PAID).map(p -> {
            return p.getAmount().multiply(feePercentage).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
        }).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal fees = donationFees.add(payoutFees);

        BigDecimal totalPayouts = payouts.stream().filter(p -> p.getStatus() != com.donations.donations.model.enums.PayoutStatus.FAILED).map(p -> {
            BigDecimal f = p.getAmount().multiply(feePercentage).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
            return p.getAmount().add(f);
        }).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal available = totalNet.subtract(totalPayouts);
        if (available.compareTo(BigDecimal.ZERO) < 0) available = BigDecimal.ZERO;

        BigDecimal pendingDonations = donations.stream().filter(d -> d.getStatus() == DonationStatus.PROCESSING || d.getStatus() == DonationStatus.PENDING).map(Donation::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal pendingPayouts = payouts.stream().filter(p -> p.getStatus() == com.donations.donations.model.enums.PayoutStatus.PENDING || p.getStatus() == com.donations.donations.model.enums.PayoutStatus.PROCESSING).map(Payout::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal pending = pendingDonations.add(pendingPayouts);

        BigDecimal paidOut = payouts.stream().filter(p -> p.getStatus() == com.donations.donations.model.enums.PayoutStatus.PAID).map(Payout::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        long donationCount = donations.stream().filter(d -> d.getStatus() == DonationStatus.SUCCESS).count();
        long supporterCount = donations.stream().filter(d -> d.getStatus() == DonationStatus.SUCCESS).map(Donation::getSupporterName).distinct().count();

        // Calculate thisMonth donations
        LocalDateTime now = LocalDateTime.now();
        BigDecimal thisMonth = donations.stream().filter(d -> d.getStatus() == DonationStatus.SUCCESS && d.getCreatedAt() != null).filter(d -> d.getCreatedAt().getYear() == now.getYear() && d.getCreatedAt().getMonthValue() == now.getMonthValue()).map(Donation::getNetAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        // Ultimos 14 dias (perDay)
        Map<String, BigDecimal> dayBuckets = new LinkedHashMap<>();
        for (int i = 13; i >= 0; i--) {
            LocalDateTime day = now.minusDays(i);
            String label = day.format(DateTimeFormatter.ofPattern("d/M"));
            dayBuckets.put(label, BigDecimal.ZERO);
        }
        for (Donation d : donations) {
            if (d.getStatus() == DonationStatus.SUCCESS && d.getCreatedAt() != null) {
                String label = d.getCreatedAt().format(DateTimeFormatter.ofPattern("d/M"));
                if (dayBuckets.containsKey(label)) {
                    dayBuckets.put(label, dayBuckets.get(label).add(d.getNetAmount()));
                }
            }
        }
        List<Map<String, Object>> perDay = dayBuckets.entrySet().stream().map(e -> Map.of("label", (Object) e.getKey(), "value", e.getValue())).collect(Collectors.toList());

        // Ultimos 6 meses (perMonth)
        Map<String, BigDecimal> monthBuckets = new LinkedHashMap<>();
        for (int i = 5; i >= 0; i--) {
            LocalDateTime m = now.minusMonths(i);
            String label = PT_MONTHS[m.getMonthValue() - 1];
            monthBuckets.put(label, BigDecimal.ZERO);
        }
        for (Donation d : donations) {
            if (d.getStatus() == DonationStatus.SUCCESS && d.getCreatedAt() != null) {
                int monthIdx = d.getCreatedAt().getMonthValue() - 1;
                String label = PT_MONTHS[monthIdx];
                if (monthBuckets.containsKey(label)) {
                    monthBuckets.put(label, monthBuckets.get(label).add(d.getNetAmount()));
                }
            }
        }
        List<Map<String, Object>> perMonth = monthBuckets.entrySet().stream().map(e -> Map.of("label", (Object) e.getKey(), "value", e.getValue())).collect(Collectors.toList());

        // Receita por metodo (byMethod) com percentual share
        Map<com.donations.donations.model.enums.PaymentMethod, BigDecimal> methodSums = donations.stream().filter(d -> d.getStatus() == DonationStatus.SUCCESS).collect(Collectors.groupingBy(Donation::getMethod, Collectors.reducing(BigDecimal.ZERO, Donation::getNetAmount, BigDecimal::add)));

        BigDecimal sumAllMethods = methodSums.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Map<String, Object>> byMethod = Arrays.stream(com.donations.donations.model.enums.PaymentMethod.values()).map(method -> {
            BigDecimal amt = methodSums.getOrDefault(method, BigDecimal.ZERO);
            double share = sumAllMethods.compareTo(BigDecimal.ZERO) > 0 ? amt.divide(sumAllMethods, 4, java.math.RoundingMode.HALF_UP).doubleValue() * 100 : 0.0;
            return Map.of("method", (Object) method.name(), "amount", amt, "value", amt, "share", (int) Math.round(share));
        }).collect(Collectors.toList());

        Map<String, Object> statsObj = new LinkedHashMap<>();
        statsObj.put("totalReceived", totalSuccess);
        statsObj.put("thisMonth", thisMonth);
        statsObj.put("donationCount", donationCount);
        statsObj.put("supporterCount", supporterCount);
        statsObj.put("available", available);
        statsObj.put("pending", pending);
        statsObj.put("fees", fees);
        statsObj.put("paidOut", paidOut);
        statsObj.put("feePercentage", feePercentage);

        return Map.of("stats", statsObj, "perDay", perDay, "perMonth", perMonth, "byMethod", byMethod, "feePercentage", feePercentage);
    }
}
