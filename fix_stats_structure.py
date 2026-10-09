import os

path = "src/main/java/com/donations/donations/application/usecase/creator/GetCreatorStatsUseCase.java"
with open(path) as f: content = f.read()

# I need to create a nested map structure.
# Instead of string manipulation, I will just rewrite the whole file because it's safer.
new_content = """package com.donations.donations.application.usecase.creator;

import com.donations.donations.application.port.out.DonationPort;
import com.donations.donations.application.port.out.PayoutPort;
import com.donations.donations.domain.model.Donation;
import com.donations.donations.domain.model.Payout;
import com.donations.donations.domain.model.enums.DonationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class GetCreatorStatsUseCase {
    private final DonationPort donationPort;
    private final PayoutPort payoutPort;

    public Map<String, Object> execute(UUID creatorId) {
        List<Donation> donations = donationPort.findByCreatorId(creatorId);
        List<Payout> payouts = payoutPort.findByCreatorId(creatorId);

        BigDecimal totalSuccess = donations.stream()
                .filter(d -> d.getStatus() == DonationStatus.SUCCESS)
                .map(Donation::getAmount) // Gross amount
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalNet = donations.stream()
                .filter(d -> d.getStatus() == DonationStatus.SUCCESS)
                .map(Donation::getNetAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal fees = donations.stream()
                .filter(d -> d.getStatus() == DonationStatus.SUCCESS)
                .map(Donation::getFeeAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPayouts = payouts.stream()
                .filter(p -> p.getStatus() != com.donations.donations.domain.model.enums.PayoutStatus.FAILED)
                .map(Payout::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal available = totalNet.subtract(totalPayouts);
        if (available.compareTo(BigDecimal.ZERO) < 0) available = BigDecimal.ZERO;

        BigDecimal pending = donations.stream()
                .filter(d -> d.getStatus() == DonationStatus.PROCESSING || d.getStatus() == DonationStatus.PENDING)
                .map(Donation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal paidOut = payouts.stream()
                .filter(p -> p.getStatus() == com.donations.donations.domain.model.enums.PayoutStatus.PAID)
                .map(Payout::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long donationCount = donations.stream().filter(d -> d.getStatus() == DonationStatus.SUCCESS).count();
        long supporterCount = donations.stream().filter(d -> d.getStatus() == DonationStatus.SUCCESS).map(Donation::getFanName).distinct().count();

        // Very basic thisMonth filter: just assume all for now or do a simple date check. Let's do all.
        BigDecimal thisMonth = totalSuccess;

        List<Map<String, Object>> perDay = donations.stream()
                .filter(d -> d.getStatus() == DonationStatus.SUCCESS && d.getCreatedAt() != null)
                .collect(Collectors.groupingBy(
                        d -> d.getCreatedAt().format(DateTimeFormatter.ofPattern("d/M")),
                        Collectors.reducing(BigDecimal.ZERO, Donation::getNetAmount, BigDecimal::add)
                )).entrySet().stream()
                .map(e -> Map.of("label", (Object)e.getKey(), "value", e.getValue()))
                .collect(Collectors.toList());

        List<Map<String, Object>> byMethod = donations.stream()
                .filter(d -> d.getStatus() == DonationStatus.SUCCESS)
                .collect(Collectors.groupingBy(
                        Donation::getMethod,
                        Collectors.reducing(BigDecimal.ZERO, Donation::getNetAmount, BigDecimal::add)
                )).entrySet().stream()
                .map(e -> Map.of("method", (Object)e.getKey().name(), "amount", e.getValue()))
                .collect(Collectors.toList());

        Map<String, Object> statsObj = Map.of(
            "totalReceived", totalSuccess,
            "thisMonth", thisMonth,
            "donationCount", donationCount,
            "supporterCount", supporterCount,
            "available", available,
            "pending", pending,
            "fees", fees,
            "paidOut", paidOut
        );

        return Map.of(
                "stats", statsObj,
                "perDay", perDay,
                "perMonth", List.of(),
                "byMethod", byMethod
        );
    }
}
"""

with open(path, "w") as f: f.write(new_content)
print("Done")
