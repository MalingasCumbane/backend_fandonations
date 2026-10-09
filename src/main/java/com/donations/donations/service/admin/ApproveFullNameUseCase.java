package com.donations.donations.service.admin;

import com.donations.donations.repository.CreatorRepository;
import com.donations.donations.model.Creator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ApproveFullNameUseCase {
    private final CreatorRepository creatorRepository;

    public Creator execute(UUID id) {
        Creator creator = creatorRepository.findAll().stream().filter(c -> c.getId().equals(id)).findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Creator not found"));

        if (creator.getRequestedFullName() != null) {
            creator.setFullName(creator.getRequestedFullName());
            creator.setRequestedFullName(null);
            creator = creatorRepository.save(creator);
        }
        return creator;
    }
}
