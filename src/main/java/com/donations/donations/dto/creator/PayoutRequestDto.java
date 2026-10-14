package com.donations.donations.dto.creator;

import com.donations.donations.model.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayoutRequestDto {
    private BigDecimal amount;
    private PaymentMethod method;
    private String phone;
    private String otp;
}
