package com.donations.donations.service.creator;

import com.donations.donations.repository.DonationRepository;
import com.donations.donations.model.Donation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetCreatorDonationsUseCase {
    private final DonationRepository donationRepository;

    public List<Donation> execute(UUID creatorId) {
        return donationRepository.findByCreatorId(creatorId).stream()
                .map(d -> Donation.builder()
                        .id(d.getId())
                        .creatorId(d.getCreatorId())
                        .campaignId(d.getCampaignId())
                        .reference(d.getReference())
                        .supporterName(d.isAnonymous() ? "Apoiador Anónimo" : d.getSupporterName())
                        .supporterEmail(null) // Ocultado por privacidade do apoiante
                        .supporterPhone(null) // Ocultado por privacidade do apoiante
                        .message(d.getMessage())
                        .anonymous(d.isAnonymous())
                        .amount(d.getAmount())
                        .feeAmount(d.getFeeAmount())
                        .netAmount(d.getNetAmount())
                        .method(d.getMethod())
                        .status(d.getStatus())
                        .failureReason(d.getFailureReason())
                        .createdAt(d.getCreatedAt())
                        .updatedAt(d.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
