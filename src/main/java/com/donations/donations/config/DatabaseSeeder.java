package com.donations.donations.config;

import com.donations.donations.model.Category;
import com.donations.donations.model.Country;
import com.donations.donations.repository.CategoryRepository;
import com.donations.donations.repository.*;
import com.donations.donations.repository.CountryRepository;
import com.donations.donations.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import com.donations.donations.model.User;
import com.donations.donations.model.VerificationToken;
import com.donations.donations.model.UserRole;
import com.donations.donations.model.PlatformSettings;
import com.donations.donations.repository.PlatformSettingsRepository;
import com.donations.donations.repository.*;
import org.springframework.beans.factory.annotation.Value;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.core.annotation.Order;


@Component
//@org.springframework.core.annotation.Order(2)
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final CountryRepository countryRepository;
    private final UserRepository userRepository;
    private final VerificationTokenRepository tokenRepository;
    private final EmailRepository emailRepository;
    private final PasswordEncoderRepository passwordEncoder;
    private final PlatformSettingsRepository platformSettingsRepository;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(String... args) {

        // Seed Platform Settings
        if (platformSettingsRepository.count() == 0) {
            PlatformSettings settings = new PlatformSettings();
            settings.setPlatformFeePercentage(new BigDecimal("10.00"));
            settings.setMinPayoutAmount(new BigDecimal("500.00"));
            settings.setPayoutsEnabled(true);
            settings.setRegistrationsOpen(true);
            platformSettingsRepository.save(settings);
        }

        // Seed Admin User
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setPasswordHash(passwordEncoder.encode(adminPassword));
            admin.setRole(UserRole.SUPER_ADMIN);
            admin.setIsActive(false); // REQUIRE ACTIVATION
            admin = userRepository.save(admin);
            
            String tokenStr = java.util.UUID.randomUUID().toString();
            VerificationToken token = new VerificationToken();
            token.setToken(tokenStr);
            token.setUserId(admin.getId());
            token.setExpiryDate(java.time.LocalDateTime.now().plusHours(24));
            tokenRepository.save(token);
            
            emailRepository.sendVerificationEmail(admin.getEmail(), tokenStr);
        }

        if (categoryRepository.count() == 0) {
            List<String> cats = List.of(
                "Influencer", "Tiktoker", "Youtuber", "Jornalismo", "Música", "Entretenimento"
            );
            
            for (String c : cats) {
                Category ent = new Category();
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
                Country ent = new Country();
                ent.setName(c[0]);
                ent.setCode(c[1]);
                ent.setPhoneCode(c[2]);
                countryRepository.save(ent);
            }
        }
    }
}
