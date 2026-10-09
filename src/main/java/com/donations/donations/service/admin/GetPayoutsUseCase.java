package com.donations.donations.service.admin;

import com.donations.donations.model.Payout;
import com.donations.donations.repository.PayoutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetPayoutsUseCase {
    private final PayoutRepository repository;

    public List<Payout> execute() {
        return repository.findAll();
    }
}
