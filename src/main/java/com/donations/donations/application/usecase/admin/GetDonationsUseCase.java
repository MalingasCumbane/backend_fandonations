package com.donations.donations.application.usecase.admin;

import com.donations.donations.domain.model.Donation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetDonationsUseCase {
    public List<Donation> execute() {
        return List.of();
    }
}
