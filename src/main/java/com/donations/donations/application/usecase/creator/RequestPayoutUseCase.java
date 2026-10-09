package com.donations.donations.application.usecase.creator;

import com.donations.donations.application.port.out.CreatorPort;
import com.donations.donations.application.port.out.DonationPort;
import com.donations.donations.application.port.out.PayoutPort;
import com.donations.donations.application.port.out.PlatformSettingsPort;
import com.donations.donations.domain.model.Creator;
import com.donations.donations.domain.model.CreatorStatus;
import com.donations.donations.domain.model.Donation;
import com.donations.donations.domain.model.Payout;
import com.donations.donations.domain.model.PlatformSettings;
import com.donations.donations.domain.model.enums.DonationStatus;
import com.donations.donations.domain.model.enums.PaymentMethod;
import com.donations.donations.domain.model.enums.PayoutStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RequestPayoutUseCase {
    private final PayoutPort payoutPort;
    private final CreatorPort creatorPort;
    private final PlatformSettingsPort settingsPort;
    private final DonationPort donationPort;

    public Payout execute(UUID creatorId, BigDecimal amount, PaymentMethod method, String phone) {
        Creator creator = creatorPort.findById(creatorId)
                .orElseThrow(() -> new IllegalArgumentException("Criador não encontrado."));

        if (creator.getStatus() != CreatorStatus.VERIFIED) {
            throw new IllegalArgumentException("O criador não está verificado.");
        }

        PlatformSettings settings = settingsPort.getSettings()
                .orElseThrow(() -> new IllegalStateException("Configurações não encontradas."));

        if (!settings.isPayoutsEnabled()) {
            throw new IllegalArgumentException("Os levantamentos estão temporariamente desativados.");
        }

        if (amount.compareTo(settings.getMinPayoutAmount()) < 0) {
            throw new IllegalArgumentException("O valor é inferior ao mínimo permitido.");
        }

        List<Donation> donations = donationPort.findByCreatorId(creatorId);
        BigDecimal totalReceived = donations.stream()
                .filter(d -> d.getStatus() == DonationStatus.SUCCESS)
                .map(Donation::getNetAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Payout> payouts = payoutPort.findByCreatorId(creatorId);
        BigDecimal paidOut = payouts.stream()
                .filter(p -> p.getStatus() == PayoutStatus.PAID || p.getStatus() == PayoutStatus.PROCESSING || p.getStatus() == PayoutStatus.PENDING)
                .map(Payout::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal available = totalReceived.subtract(paidOut);

        if (amount.compareTo(available) > 0) {
            throw new IllegalArgumentException("Saldo insuficiente.");
        }

        Payout payout = Payout.builder()
                .creatorId(creatorId)
                .amount(amount)
                .method(method)
                .phone(phone)
                .status(PayoutStatus.PENDING)
                .requestedAt(LocalDateTime.now())
                .build();

        return payoutPort.save(payout);
    }
}
