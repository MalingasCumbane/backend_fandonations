package com.donations.donations.application.usecase;

import com.donations.donations.application.dto.SignupCommand;
import com.donations.donations.application.port.out.EmailPort;
import com.donations.donations.application.port.out.PasswordEncoderPort;
import com.donations.donations.domain.model.User;
import com.donations.donations.domain.model.UserRole;
import com.donations.donations.domain.model.VerificationToken;
import com.donations.donations.domain.repository.UserRepository;
import com.donations.donations.domain.repository.VerificationTokenRepository;
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
    private final PasswordEncoderPort passwordEncoder;
    private final EmailPort emailPort;
    private final com.donations.donations.application.port.out.CreatorPort creatorPort;

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
            com.donations.donations.domain.model.Creator emptyCreator = com.donations.donations.domain.model.Creator.builder()
                .userId(user.getId())
                .fullName(command.getName())
                .email(user.getEmail())
                .username(command.getName().toLowerCase().replaceAll("\\s+", "") + "-" + UUID.randomUUID().toString().substring(0, 5))
                .status(com.donations.donations.domain.model.CreatorStatus.DRAFT)
                .build();
            creatorPort.save(emptyCreator);
        }

        String tokenStr = UUID.randomUUID().toString();
        VerificationToken token = VerificationToken.builder()
                .token(tokenStr)
                .userId(user.getId())
                .expiryDate(LocalDateTime.now().plusHours(24))
                .build();

        tokenRepository.save(token);
        emailPort.sendVerificationEmail(user.getEmail(), tokenStr);
    }
}
