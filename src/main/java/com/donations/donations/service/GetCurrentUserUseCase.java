package com.donations.donations.service;

import com.donations.donations.model.User;
import com.donations.donations.repository.UserRepository;
import com.donations.donations.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetCurrentUserUseCase {

    private final UserRepository userRepository;

    public User execute(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
