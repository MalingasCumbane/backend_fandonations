package com.donations.donations.presentation.rest.dto.admin;

import com.donations.donations.domain.model.enums.PayoutStatus;
import lombok.Data;

@Data
public class ReviewPayoutRequestDto {
    private PayoutStatus status;
    private String adminNote;
    private String transactionReference;
}
