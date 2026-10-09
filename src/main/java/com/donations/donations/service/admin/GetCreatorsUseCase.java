package com.donations.donations.service.admin;

import com.donations.donations.repository.CreatorRepository;
import com.donations.donations.model.Creator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetCreatorsUseCase {
    private final CreatorRepository creatorRepository;

    public List<Creator> execute() {
        return creatorRepository.findAll();
    }
}
