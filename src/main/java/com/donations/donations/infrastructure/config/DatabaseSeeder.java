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

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final CategoryJpaRepository categoryRepository;
    private final CountryJpaRepository countryRepository;

    @Override
    @Transactional
    public void run(String... args) {
        // We will reset the tables to ensure exactly the requested lists exist in early development
        categoryRepository.deleteAll();
        countryRepository.deleteAll();

        List<String> cats = List.of(
            "Influencer", "Tiktoker", "Youtuber", "Jornalismo", "Música", "Entretenimento"
        );
        
        for (String c : cats) {
            CategoryJpaEntity ent = new CategoryJpaEntity();
            ent.setName(c);
            categoryRepository.save(ent);
        }

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
