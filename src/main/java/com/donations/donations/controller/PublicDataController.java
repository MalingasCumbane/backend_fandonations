package com.donations.donations.controller;

import com.donations.donations.model.Campaign;
import com.donations.donations.model.Category;
import com.donations.donations.model.Country;
import com.donations.donations.model.Report;
import com.donations.donations.model.CreatorStatus;
import com.donations.donations.model.enums.DonationStatus;
import com.donations.donations.model.enums.PaymentMethod;
import com.donations.donations.dto.publics.*;
import com.donations.donations.repository.CategoryRepository;
import com.donations.donations.repository.CountryRepository;
import com.donations.donations.repository.CreatorRepository;
import com.donations.donations.service.publics.ProcessDonationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;


import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicDataController {

    private final CountryRepository countryRepository;
    private final CategoryRepository categoryRepository;
    private final CreatorRepository creatorJpaRepository;
    private final ProcessDonationUseCase processDonationUseCase;
    private final com.donations.donations.repository.CampaignRepository campaignRepository;
    private final com.donations.donations.repository.PlatformSettingsRepository platformSettingsRepository;
    private final com.donations.donations.repository.DonationRepository donationRepository;


    // Controllers will be implemented here. For now, returning empty to allow compiling and check.
    @GetMapping("/creators")
    public com.donations.donations.dto.PageResponse<PublicCreator> getCreators(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        String queryLower = q != null ? q.trim().toLowerCase() : null;
        String catLower = category != null && !category.equalsIgnoreCase("Todos") ? category.trim().toLowerCase() : null;

        List<PublicCreator> all = creatorJpaRepository.findAll().stream()
                .filter(c -> c.getStatus() != null && c.getStatus().name().equals("VERIFIED"))
                .filter(c -> {
                    if (catLower == null || catLower.isBlank()) return true;
                    return c.getCategory() != null && c.getCategory().getName() != null && c.getCategory().getName().toLowerCase().contains(catLower);
                })
                .filter(c -> {
                    if (queryLower == null || queryLower.isBlank()) return true;
                    String fullName = c.getFullName() != null ? c.getFullName().toLowerCase() : "";
                    String username = c.getUsername() != null ? c.getUsername().toLowerCase() : "";
                    String bio = c.getBio() != null ? c.getBio().toLowerCase() : "";
                    return fullName.contains(queryLower) || username.contains(queryLower) || bio.contains(queryLower);
                })
                .map(c -> PublicCreator.builder()
                        .id(c.getId())
                        .username(c.getUsername())
                        .fullName(c.getFullName())
                        .category(c.getCategory() != null ? c.getCategory().getName() : null)
                        .bio(c.getBio())
                        .country(c.getCountry() != null ? c.getCountry().getName() : null)
                        .avatarUrl(c.getAvatarUrl())
                        .status(c.getStatus())
                        .createdAt(c.getCreatedAt())
                        .socials(PublicCreator.Socials.builder()
                                .facebook(c.getFacebookUrl())
                                .twitter(c.getTwitterUrl())
                                .instagram(c.getInstagramUrl())
                                .youtube(c.getYoutubeUrl())
                                .tiktok(c.getTiktokUrl())
                                .build())
                        .build())
                .collect(Collectors.toList());

        return com.donations.donations.dto.PageResponse.of(all, page, size);
    }

    @GetMapping("/creators/{username}")
    public PublicCreatorProfile getCreator(@PathVariable String username) {
        return creatorJpaRepository.findByUsername(username)
                .map(c -> {
                    // Check if VERIFIED
                    if (c.getStatus() == null || !c.getStatus().name().equals("VERIFIED")) {
                        throw new java.util.NoSuchElementException("Creator not found");
                    }

                    PublicCreator publicCreator = PublicCreator.builder()
                            .id(c.getId())
                            .userId(c.getUserId())
                            .username(c.getUsername())
                            .fullName(c.getFullName())
                            .category(c.getCategory() != null ? c.getCategory().getName() : null)
                            .bio(c.getBio())
                            .country(c.getCountry() != null ? c.getCountry().getName() : null)
                            .avatarUrl(c.getAvatarUrl())
                            .status(c.getStatus())
                            .createdAt(c.getCreatedAt())
                            .socials(PublicCreator.Socials.builder()
                                    .facebook(c.getFacebookUrl())
                                    .twitter(c.getTwitterUrl())
                                    .instagram(c.getInstagramUrl())
                                    .youtube(c.getYoutubeUrl())
                                    .tiktok(c.getTiktokUrl())
                                    .build())
                            .build();

                    List<Campaign> campaigns = campaignRepository.findByCreatorId(c.getId()).stream()
                            .filter(camp -> camp.getStatus() != null && camp.getStatus().name().equals("ACTIVE"))
                            .collect(Collectors.toList());

                    List<com.donations.donations.model.Donation> creatorDonations = donationRepository.findByCreatorId(c.getId());
                    long donationCount = creatorDonations.stream()
                            .filter(d -> d.getStatus() == DonationStatus.SUCCESS)
                            .count();
                    long supporterCount = creatorDonations.stream()
                            .filter(d -> d.getStatus() == DonationStatus.SUCCESS)
                            .map(d -> (d.getSupporterEmail() != null && !d.getSupporterEmail().isBlank())
                                    ? d.getSupporterEmail().trim().toLowerCase()
                                    : ((d.getSupporterName() != null && !d.getSupporterName().isBlank())
                                            ? d.getSupporterName().trim().toLowerCase()
                                            : d.getId().toString()))
                            .distinct()
                            .count();

                    return PublicCreatorProfile.builder()
                            .creator(publicCreator)
                            .campaigns(campaigns)
                            .stats(java.util.Map.of("supporterCount", (int) supporterCount, "donationCount", (int) donationCount))
                            .build();
                })
                .orElseThrow(() -> new java.util.NoSuchElementException("Creator not found"));
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        long verifiedCreators = creatorJpaRepository.findAll().stream()
                .filter(c -> c.getStatus() == CreatorStatus.VERIFIED)
                .count();
        long totalCreators = creatorJpaRepository.count();

        List<com.donations.donations.model.Donation> allDonations = donationRepository.findAll();
        long confirmedDonations = allDonations.stream()
                .filter(d -> d.getStatus() == DonationStatus.SUCCESS)
                .count();

        long activeCampaigns = campaignRepository.findAll().stream()
                .filter(c -> c.getStatus() != null && c.getStatus().name().equals("ACTIVE"))
                .count();

        long uniqueSupporters = allDonations.stream()
                .filter(d -> d.getStatus() == DonationStatus.SUCCESS)
                .map(d -> (d.getSupporterEmail() != null && !d.getSupporterEmail().isBlank())
                        ? d.getSupporterEmail().trim().toLowerCase()
                        : ((d.getSupporterName() != null && !d.getSupporterName().isBlank())
                                ? d.getSupporterName().trim().toLowerCase()
                                : d.getId().toString()))
                .distinct()
                .count();

        Map<String, Object> stats = new java.util.LinkedHashMap<>();
        stats.put("creators", verifiedCreators > 0 ? verifiedCreators : totalCreators);
        stats.put("totalCreators", totalCreators);
        stats.put("verified", verifiedCreators);
        stats.put("donations", confirmedDonations);
        stats.put("campaigns", activeCampaigns);
        stats.put("activeCampaigns", activeCampaigns);
        stats.put("supporters", uniqueSupporters);

        return stats;
    }

    @GetMapping("/settings")
    public Map<String, Object> getSettings() {
        return platformSettingsRepository.getSettings()
                .map(s -> Map.<String, Object>of(
                        "platformFeePercentage", s.getPlatformFeePercentage() != null ? s.getPlatformFeePercentage() : 10,
                        "minPayoutAmount", s.getMinPayoutAmount() != null ? s.getMinPayoutAmount() : 500,
                        "registrationsOpen", s.isRegistrationsOpen(),
                        "payoutsEnabled", s.isPayoutsEnabled()
                ))
                .orElseGet(() -> Map.of(
                        "platformFeePercentage", 10,
                        "minPayoutAmount", 500,
                        "registrationsOpen", true,
                        "payoutsEnabled", true
                ));
    }


    @GetMapping("/countries")
    public List<Country> getCountries() {
        return countryRepository.findAll();
    }

    @GetMapping("/categories")
    public List<Category> getCategories() {
        return categoryRepository.findAll();
    }


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
        com.donations.donations.model.Donation donation = donationRepository.findByReference(reference)
                .orElseThrow(() -> new java.util.NoSuchElementException("Doação não encontrada com a referência: " + reference));

        String creatorUsername = creatorJpaRepository.findById(donation.getCreatorId())
                .map(com.donations.donations.model.Creator::getUsername)
                .orElse(null);

        return PublicDonationStatus.builder()
                .reference(donation.getReference())
                .status(donation.getStatus())
                .amount(donation.getAmount())
                .method(donation.getMethod())
                .failureReason(donation.getFailureReason())
                .creatorUsername(creatorUsername)
                .build();
    }

    @GetMapping(value = "/donations/{reference}/pdf", produces = "application/pdf")
    public org.springframework.http.ResponseEntity<byte[]> downloadDonationPdf(@PathVariable String reference) {
        com.donations.donations.model.Donation donation = donationRepository.findByReference(reference)
                .orElseThrow(() -> new java.util.NoSuchElementException("Doação não encontrada com a referência: " + reference));

        String creatorName = creatorJpaRepository.findById(donation.getCreatorId())
                .map(com.donations.donations.model.Creator::getFullName)
                .orElse("Criador");

        byte[] pdfBytes = com.donations.donations.util.PdfReceiptGenerator.generateReceipt(
                donation.getSupporterEmail(),
                creatorName,
                donation.getAmount() != null ? donation.getAmount().stripTrailingZeros().toPlainString() : "0",
                donation.getReference(),
                donation.getMethod() != null ? donation.getMethod().name() : "M-Pesa / e-Mola"
        );

        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Recibo-" + reference + ".pdf\"")
                .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, "application/pdf")
                .body(pdfBytes);
    }

    @PostMapping("/reports")
    @ResponseStatus(HttpStatus.CREATED)
    public Report createReport(@RequestBody ReportRequestDto request) {
        return Report.builder().build(); // Assuming Report domain has builder
    }
}
