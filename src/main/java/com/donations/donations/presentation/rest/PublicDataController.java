package com.donations.donations.presentation.rest;

import com.donations.donations.infrastructure.persistence.repository.CategoryJpaRepository;
import com.donations.donations.infrastructure.persistence.repository.CountryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicDataController {

    private final CategoryJpaRepository categoryRepository;
    private final CountryJpaRepository countryRepository;

    @GetMapping("/categories")
    public ResponseEntity<?> getCategories() {
        return ResponseEntity.ok(
            categoryRepository.findAllByIsActiveTrueAndIsDeletedFalseOrderByNameAsc()
                .stream().map(c -> java.util.Map.of("id", c.getId(), "name", c.getName()))
                .collect(Collectors.toList())
        );
    }

    @GetMapping("/countries")
    public ResponseEntity<?> getCountries() {
        return ResponseEntity.ok(
            countryRepository.findAllByIsActiveTrueAndIsDeletedFalseOrderByNameAsc()
                .stream().map(c -> java.util.Map.of("id", c.getId(), "name", c.getName(), "code", c.getCode(), "phoneCode", c.getPhoneCode()))
                .collect(Collectors.toList())
        );
    }
}
