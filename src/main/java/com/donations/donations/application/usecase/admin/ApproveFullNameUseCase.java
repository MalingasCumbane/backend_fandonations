package com.donations.donations.application.usecase.admin;

import com.donations.donations.application.port.out.CreatorPort;
import com.donations.donations.domain.model.Creator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ApproveFullNameUseCase {
    private final CreatorPort creatorPort;

    public Creator execute(UUID id) {
        Creator creator = creatorPort.findAll().stream().filter(c -> c.getId().equals(id)).findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Creator not found"));

        if (creator.getRequestedFullName() != null) {
            creator.setFullName(creator.getRequestedFullName());
            creator.setRequestedFullName(null);
            creator = creatorPort.save(creator);
        }
        return creator;
    }
}
