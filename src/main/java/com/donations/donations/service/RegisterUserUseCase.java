package com.donations.donations.service;

import com.donations.donations.dto.SignupCommand;
import com.donations.donations.repository.CreatorRepository;
import com.donations.donations.repository.*;
import com.donations.donations.repository.EmailRepository;
import com.donations.donations.repository.*;
import com.donations.donations.repository.PasswordEncoderRepository;
import com.donations.donations.repository.*;
import com.donations.donations.model.*;
import com.donations.donations.repository.UserRepository;
import com.donations.donations.repository.*;
import com.donations.donations.repository.VerificationTokenRepository;
import com.donations.donations.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegisterUserUseCase {

    private final UserRepository userRepository;
    private final VerificationTokenRepository tokenRepository;
    private final PasswordEncoderRepository passwordEncoder;
    private final EmailRepository emailRepository;
    private final CreatorRepository creatorRepository;

    @Transactional
    public void execute(SignupCommand command) {
        if (userRepository.findByEmail(command.getEmail()).isPresent()) {
            throw new RuntimeException("Email is already in use");
        }

        User user = User.builder()
                .email(command.getEmail())
                .passwordHash(passwordEncoder.encode(command.getPassword()))
                .role(command.getRole() != null ? command.getRole() : UserRole.CREATOR)
                .active(false)
                .build();

        user = userRepository.save(user);

        if (user.getRole() == UserRole.CREATOR) {
            Creator emptyCreator = Creator.builder()
                .userId(user.getId())
                .fullName(command.getName())
                .email(user.getEmail())
                .username(command.getName().toLowerCase().replaceAll("\\s+", "") + "-" + UUID.randomUUID().toString().substring(0, 5))
                .status(CreatorStatus.DRAFT)
                .build();
            creatorRepository.save(emptyCreator);
        }

        String tokenStr = UUID.randomUUID().toString();
        VerificationToken token = VerificationToken.builder()
                .token(tokenStr)
                .userId(user.getId())
                .expiryDate(LocalDateTime.now().plusHours(24))
                .build();

        tokenRepository.save(token);
        emailRepository.sendVerificationEmail(user.getEmail(), tokenStr);
    }
}
