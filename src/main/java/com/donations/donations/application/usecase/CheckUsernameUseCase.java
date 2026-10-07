package com.donations.donations.application.usecase;

import com.donations.donations.application.port.out.CreatorPort;
import com.donations.donations.domain.model.Creator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CheckUsernameUseCase {

    private final CreatorPort creatorPort;

    public boolean execute(String username, UUID excludeUserId) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        Optional<Creator> existing = creatorPort.findByUsername(username);
        if (existing.isPresent()) {
            // It's available if it belongs to the current user
            return existing.get().getUserId().equals(excludeUserId);
        }
        return true;
    }
}
