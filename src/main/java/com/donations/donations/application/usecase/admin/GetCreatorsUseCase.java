package com.donations.donations.application.usecase.admin;

import com.donations.donations.application.port.out.CreatorPort;
import com.donations.donations.domain.model.Creator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetCreatorsUseCase {
    private final CreatorPort creatorPort;

    public List<Creator> execute() {
        return creatorPort.findAll();
    }
}
