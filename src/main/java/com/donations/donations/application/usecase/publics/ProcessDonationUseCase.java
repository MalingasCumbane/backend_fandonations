package com.donations.donations.application.usecase.publics;

import com.donations.donations.domain.model.enums.PaymentMethod;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

public interface ProcessDonationUseCase {
    DonationResult process(DonationCommand command);

    @Data
    @Builder
    class DonationCommand {
        private UUID creatorId;
        private UUID campaignId;
        private BigDecimal amount;
        private PaymentMethod method;
        private String phone;
        private String message;
        private boolean anonymous;
        private String supporterName;
        private String supporterEmail;
    }

    @Data
    @Builder
    class DonationResult {
        private String reference;
        private String status;
        private BigDecimal amount;
        private String method;
        private String failureReason;
        private String creatorUsername;
    }
}
