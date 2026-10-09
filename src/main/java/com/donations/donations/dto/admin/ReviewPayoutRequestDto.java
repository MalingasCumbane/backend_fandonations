package com.donations.donations.dto.admin;

import com.donations.donations.model.enums.PayoutStatus;
import lombok.Data;

@Data
public class ReviewPayoutRequestDto {
    private PayoutStatus status;
    private String adminNote;
    private String transactionReference;
}
