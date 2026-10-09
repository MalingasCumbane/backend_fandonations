package com.donations.donations.application.usecase.impl;

import com.donations.donations.application.port.out.CampaignPort;
import com.donations.donations.application.port.out.CreatorPort;
import com.donations.donations.application.port.out.DonationPort;
import com.donations.donations.application.usecase.publics.ProcessDonationUseCase;
import com.donations.donations.domain.model.Campaign;
import com.donations.donations.domain.model.Creator;
import com.donations.donations.domain.model.CreatorStatus;
import com.donations.donations.domain.model.Donation;
import com.donations.donations.domain.model.enums.CampaignStatus;
import com.donations.donations.domain.model.enums.DonationStatus;
import com.donations.donations.domain.model.enums.PaymentMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProcessDonationUseCaseImpl implements ProcessDonationUseCase {

    private final CreatorPort creatorPort;
    private final CampaignPort campaignPort;
    private final DonationPort donationPort;
    private final AsyncPaymentProcessor asyncPaymentProcessor;

    @Override
    public DonationResult process(DonationCommand command) {
        Creator creator = creatorPort.findById(command.getCreatorId())
                .orElseThrow(() -> new IllegalArgumentException("Criador não encontrado."));

        if (creator.getStatus() != CreatorStatus.VERIFIED) {
            throw new IllegalArgumentException("O criador não está verificado.");
        }

        if (command.getCampaignId() != null) {
            Campaign campaign = campaignPort.findById(command.getCampaignId())
                    .orElseThrow(() -> new IllegalArgumentException("Campanha não encontrada."));
            if (campaign.getStatus() != CampaignStatus.ACTIVE) {
                throw new IllegalArgumentException("A campanha não está ativa.");
            }
            if (!campaign.getCreatorId().equals(creator.getId())) {
                throw new IllegalArgumentException("A campanha não pertence a este criador.");
            }
        }

        if (command.getAmount().compareTo(BigDecimal.TEN) < 0) {
            throw new IllegalArgumentException("O valor mínimo de doação é 10 MZN.");
        }

        if (command.getMethod() == PaymentMethod.MPESA) {
            if (!command.getPhone().matches("^(84|85)\\d{7}$")) {
                throw new IllegalArgumentException("Número M-Pesa inválido.");
            }
        } else if (command.getMethod() == PaymentMethod.EMOLA) {
            if (!command.getPhone().matches("^(86|87)\\d{7}$")) {
                throw new IllegalArgumentException("Número e-Mola inválido.");
            }
        }

        Donation donation = Donation.builder()
                .creatorId(creator.getId())
                .campaignId(command.getCampaignId())
                .amount(command.getAmount())
                .feeAmount(BigDecimal.ZERO) // We could apply platform fee calculation here
                .netAmount(command.getAmount())
                .method(command.getMethod())
                .status(DonationStatus.PROCESSING)
                .anonymous(command.isAnonymous())
                .message(command.getMessage())
                .supporterName(command.getSupporterName())
                .supporterEmail(command.getSupporterEmail())
                .supporterPhone(command.getPhone())
                .reference(UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .build();

        donation = donationPort.save(donation);

        asyncPaymentProcessor.processPayment(donation, command.getPhone());

        return DonationResult.builder()
                .reference(donation.getReference())
                .status(donation.getStatus().name())
                .amount(donation.getAmount())
                .method(donation.getMethod().name())
                .creatorUsername(creator.getUsername())
                .build();
    }
}
