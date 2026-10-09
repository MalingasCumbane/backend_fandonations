package com.donations.donations.presentation.rest;

import com.donations.donations.domain.model.Campaign;
import com.donations.donations.domain.model.Report;
import com.donations.donations.domain.model.enums.DonationStatus;
import com.donations.donations.domain.model.enums.PaymentMethod;
import com.donations.donations.infrastructure.persistence.entity.CategoryJpaEntity;
import com.donations.donations.infrastructure.persistence.entity.CountryJpaEntity;
import com.donations.donations.infrastructure.persistence.repository.CategoryJpaRepository;
import com.donations.donations.infrastructure.persistence.repository.CountryJpaRepository;
import com.donations.donations.presentation.rest.dto.publics.*;
import com.donations.donations.application.usecase.publics.ProcessDonationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.donations.donations.infrastructure.persistence.repository.CreatorJpaRepository;

import java.util.stream.Collectors;


import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicDataController {

    private final CountryJpaRepository countryRepository;
    private final CategoryJpaRepository categoryRepository;

    // Controllers will be implemented here. For now, returning empty to allow compiling and check.
    @GetMapping("/creators")
    public List<PublicCreator> getCreators(@RequestParam(required = false) String q, @RequestParam(required = false) String category) {
        return creatorJpaRepository.findAll().stream()
                .filter(c -> c.getStatus() != null && c.getStatus().name().equals("VERIFIED"))
                .map(c -> PublicCreator.builder()
                        .id(c.getId())
                        .username(c.getUsername())
                        .fullName(c.getFullName())
                        .category(c.getCategory() != null ? c.getCategory().getName() : null)
                        .bio(c.getBio())
                        .country(c.getCountry() != null ? c.getCountry().getName() : null)
                        .build())
                .collect(Collectors.toList());
    }

    @GetMapping("/creators/{username}")
    public PublicCreator getCreator(@PathVariable String username) {
        return creatorJpaRepository.findByUsername(username)
                .map(c -> PublicCreator.builder()
                        .id(c.getId())
                        .username(c.getUsername())
                        .fullName(c.getFullName())
                        .category(c.getCategory() != null ? c.getCategory().getName() : null)
                        .bio(c.getBio())
                        .country(c.getCountry() != null ? c.getCountry().getName() : null)
                        .build())
                .orElseThrow(() -> new java.util.NoSuchElementException("Creator not found"));
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        return Map.of();
    }

    @GetMapping("/settings")
    public Map<String, Object> getSettings() {
        return Map.of();
    }


    @GetMapping("/countries")
    public List<CountryJpaEntity> getCountries() {
        return countryRepository.findAll();
    }

    @GetMapping("/categories")
    public List<CategoryJpaEntity> getCategories() {
        return categoryRepository.findAll();
    }

    private final CreatorJpaRepository creatorJpaRepository;

    private final ProcessDonationUseCase processDonationUseCase;

    @PostMapping("/donations")
    @ResponseStatus(HttpStatus.CREATED)
    public PublicDonationStatus createDonation(@RequestBody DonationRequestDto request) {
        ProcessDonationUseCase.DonationCommand command = ProcessDonationUseCase.DonationCommand.builder()
                .creatorId(request.getCreatorId())
                .campaignId(request.getCampaignId())
                .amount(request.getAmount())
                .method(request.getMethod())
                .phone(request.getPhone())
                .message(request.getMessage())
                .anonymous(request.isAnonymous())
                .supporterName(request.getSupporterName())
                .supporterEmail(request.getSupporterEmail())
                .build();

        ProcessDonationUseCase.DonationResult result = processDonationUseCase.process(command);

        return PublicDonationStatus.builder()
                .reference(result.getReference())
                .status(DonationStatus.valueOf(result.getStatus()))
                .amount(result.getAmount())
                .method(PaymentMethod.valueOf(result.getMethod()))
                .failureReason(result.getFailureReason())
                .creatorUsername(result.getCreatorUsername())
                .build();
    }

    @GetMapping("/donations/{reference}")
    public PublicDonationStatus getDonation(@PathVariable String reference) {
        return PublicDonationStatus.builder().build();
    }

    @PostMapping("/reports")
    @ResponseStatus(HttpStatus.CREATED)
    public Report createReport(@RequestBody ReportRequestDto request) {
        return Report.builder().build(); // Assuming Report domain has builder
    }
}
