package com.donations.donations.application.port.out;

import com.donations.donations.domain.model.Payment;
import java.util.Optional;
import java.util.UUID;

public interface PaymentPort {
    Payment save(Payment payment);
    Optional<Payment> findById(UUID id);
}
