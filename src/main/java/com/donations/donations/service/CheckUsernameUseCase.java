package com.donations.donations.service;

import com.donations.donations.repository.CreatorRepository;
import com.donations.donations.model.Creator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CheckUsernameUseCase {

    private final CreatorRepository creatorRepository;

    public boolean execute(String username, UUID excludeUserId) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        Optional<Creator> existing = creatorRepository.findByUsername(username);
        if (existing.isPresent()) {
            // It's available if it belongs to the current user
            return existing.get().getUserId().equals(excludeUserId);
        }
        return true;
    }
}
