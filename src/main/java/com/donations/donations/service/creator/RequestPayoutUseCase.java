package com.donations.donations.service.creator;

import com.donations.donations.repository.CreatorRepository;
import com.donations.donations.repository.DonationRepository;
import com.donations.donations.repository.PayoutRepository;
import com.donations.donations.repository.PlatformSettingsRepository;
import com.donations.donations.model.Creator;
import com.donations.donations.model.CreatorStatus;
import com.donations.donations.model.Donation;
import com.donations.donations.model.Payout;
import com.donations.donations.model.PlatformSettings;
import com.donations.donations.model.enums.DonationStatus;
import com.donations.donations.model.enums.PaymentMethod;
import com.donations.donations.model.enums.PayoutStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RequestPayoutUseCase {
    private final PayoutRepository payoutRepository;
    private final CreatorRepository creatorRepository;
    private final PlatformSettingsRepository settingsRepository;
    private final DonationRepository donationRepository;

    public Payout execute(UUID creatorId, BigDecimal amount, PaymentMethod method, String phone) {
        Creator creator = creatorRepository.findById(creatorId)
                .orElseThrow(() -> new IllegalArgumentException("Criador não encontrado."));

        if (creator.getStatus() != CreatorStatus.VERIFIED) {
            throw new IllegalArgumentException("O criador não está verificado.");
        }

        PlatformSettings settings = settingsRepository.getSettings()
                .orElseThrow(() -> new IllegalStateException("Configurações não encontradas."));

        if (!settings.isPayoutsEnabled()) {
            throw new IllegalArgumentException("Os levantamentos estão temporariamente desativados.");
        }

        if (amount.compareTo(settings.getMinPayoutAmount()) < 0) {
            throw new IllegalArgumentException("O valor é inferior ao mínimo permitido.");
        }

        List<Donation> donations = donationRepository.findByCreatorId(creatorId);
        BigDecimal totalReceived = donations.stream()
                .filter(d -> d.getStatus() == DonationStatus.SUCCESS)
                .map(Donation::getNetAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Payout> payouts = payoutRepository.findByCreatorId(creatorId);
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

        return payoutRepository.save(payout);
    }
}
