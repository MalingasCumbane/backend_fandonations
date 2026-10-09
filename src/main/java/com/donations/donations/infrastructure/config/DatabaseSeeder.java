package com.donations.donations.infrastructure.config;

import com.donations.donations.infrastructure.persistence.entity.CategoryJpaEntity;
import com.donations.donations.infrastructure.persistence.entity.CountryJpaEntity;
import com.donations.donations.infrastructure.persistence.repository.CategoryJpaRepository;
import com.donations.donations.infrastructure.persistence.repository.CountryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import com.donations.donations.infrastructure.persistence.entity.UserJpaEntity;
import com.donations.donations.infrastructure.persistence.entity.VerificationTokenJpaEntity;
import com.donations.donations.infrastructure.persistence.repository.SpringDataUserRepo;
import com.donations.donations.infrastructure.persistence.repository.SpringDataVerificationTokenRepo;
import com.donations.donations.domain.model.UserRole;
import com.donations.donations.application.port.out.EmailPort;
import com.donations.donations.application.port.out.PasswordEncoderPort;
import com.donations.donations.infrastructure.persistence.entity.PlatformSettingsJpaEntity;
import com.donations.donations.infrastructure.persistence.repository.PlatformSettingsJpaRepository;
import org.springframework.beans.factory.annotation.Value;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.core.annotation.Order;


@Component
//@org.springframework.core.annotation.Order(2)
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final CategoryJpaRepository categoryRepository;
    private final CountryJpaRepository countryRepository;
    private final SpringDataUserRepo userRepository;
    private final SpringDataVerificationTokenRepo tokenRepository;
    private final EmailPort emailPort;
    private final PasswordEncoderPort passwordEncoder;
    private final PlatformSettingsJpaRepository platformSettingsRepository;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(String... args) {

        // Seed Platform Settings
        if (platformSettingsRepository.count() == 0) {
            PlatformSettingsJpaEntity settings = new PlatformSettingsJpaEntity();
            settings.setPlatformFeePercentage(new BigDecimal("10.00"));
            settings.setMinPayoutAmount(new BigDecimal("500.00"));
            settings.setPayoutsEnabled(true);
            settings.setRegistrationsOpen(true);
            platformSettingsRepository.save(settings);
        }

        // Seed Admin User
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            UserJpaEntity admin = new UserJpaEntity();
            admin.setEmail(adminEmail);
            admin.setPasswordHash(passwordEncoder.encode(adminPassword));
            admin.setRole(UserRole.SUPER_ADMIN);
            admin.setIsActive(false); // REQUIRE ACTIVATION
            admin = userRepository.save(admin);
            
            String tokenStr = java.util.UUID.randomUUID().toString();
            VerificationTokenJpaEntity token = new VerificationTokenJpaEntity();
            token.setToken(tokenStr);
            token.setUserId(admin.getId());
            token.setExpiryDate(java.time.LocalDateTime.now().plusHours(24));
            tokenRepository.save(token);
            
            emailPort.sendVerificationEmail(admin.getEmail(), tokenStr);
        }

        if (categoryRepository.count() == 0) {
            List<String> cats = List.of(
                "Influencer", "Tiktoker", "Youtuber", "Jornalismo", "Música", "Entretenimento"
            );
            
            for (String c : cats) {
                CategoryJpaEntity ent = new CategoryJpaEntity();
                ent.setName(c);
                categoryRepository.save(ent);
            }
        }

        if (countryRepository.count() == 0) {
            List<String[]> countries = List.of(
                new String[]{"Moçambique", "MZ", "+258"},
                new String[]{"África do Sul", "ZA", "+27"},
                new String[]{"Senegal", "SN", "+221"},
                new String[]{"Botswana", "BW", "+267"},
                new String[]{"USA", "US", "+1"},
                new String[]{"Canada", "CA", "+1"}
            );
            
            for (String[] c : countries) {
                CountryJpaEntity ent = new CountryJpaEntity();
                ent.setName(c[0]);
                ent.setCode(c[1]);
                ent.setPhoneCode(c[2]);
                countryRepository.save(ent);
            }
        }
    }
}
