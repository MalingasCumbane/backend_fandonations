package com.donations.donations.application.usecase.admin;

import com.donations.donations.domain.model.Payout;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetPayoutsUseCase {
    public List<Payout> execute() {
        return List.of();
    }
}
