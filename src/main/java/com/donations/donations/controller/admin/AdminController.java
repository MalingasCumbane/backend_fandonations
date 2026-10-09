package com.donations.donations.controller.admin;

import com.donations.donations.service.admin.*;
import com.donations.donations.model.*;
import com.donations.donations.dto.admin.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'FINANCE', 'COMPLIANCE', 'SUPPORT')")
public class AdminController {

    private final GetAdminStatsUseCase getAdminStatsUseCase;
    private final GetCreatorsUseCase getCreatorsUseCase;
    private final ReviewCreatorUseCase reviewCreatorUseCase;
    private final ApproveFullNameUseCase approveFullNameUseCase;
    private final GetVerificationsUseCase getVerificationsUseCase;
    private final GetDonationsUseCase getDonationsUseCase;
    private final GetPayoutsUseCase getPayoutsUseCase;
    private final ReviewPayoutUseCase reviewPayoutUseCase;
    private final GetReportsUseCase getReportsUseCase;
    private final ReviewReportUseCase reviewReportUseCase;
    private final GetPlatformSettingsUseCase getPlatformSettingsUseCase;
    private final UpdatePlatformSettingsUseCase updatePlatformSettingsUseCase;

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponseDto> getStats() {
        return ResponseEntity.ok(getAdminStatsUseCase.execute());
    }

    @GetMapping("/creators")
    public ResponseEntity<List<Creator>> getCreators() {
        return ResponseEntity.ok(getCreatorsUseCase.execute());
    }

    @PostMapping("/creators/{id}/review")
    public ResponseEntity<Creator> reviewCreator(@PathVariable UUID id, @RequestBody ReviewCreatorRequestDto dto) {
        return ResponseEntity.ok(reviewCreatorUseCase.execute(id, dto.getStatus(), dto.getAdminNote()));
    }

    @PostMapping("/creators/{id}/approve-full-name")
    public ResponseEntity<Creator> approveFullName(@PathVariable UUID id) {
        return ResponseEntity.ok(approveFullNameUseCase.execute(id));
    }

    @GetMapping("/verifications")
    public ResponseEntity<List<VerificationRequest>> getVerifications() {
        return ResponseEntity.ok(getVerificationsUseCase.execute());
    }

    @GetMapping("/donations")
    public ResponseEntity<List<Donation>> getDonations() {
        return ResponseEntity.ok(getDonationsUseCase.execute());
    }

    @GetMapping("/payouts")
    public ResponseEntity<List<Payout>> getPayouts() {
        return ResponseEntity.ok(getPayoutsUseCase.execute());
    }

    @PostMapping("/payouts/{id}/status")
    public ResponseEntity<Payout> reviewPayout(@PathVariable UUID id, @RequestBody ReviewPayoutRequestDto dto) {
        return ResponseEntity.ok(reviewPayoutUseCase.execute(id, dto.getStatus(), dto.getAdminNote(), dto.getTransactionReference()));
    }

    @GetMapping("/reports")
    public ResponseEntity<List<Report>> getReports() {
        return ResponseEntity.ok(getReportsUseCase.execute());
    }

    @PostMapping("/reports/{id}/status")
    public ResponseEntity<Report> reviewReport(@PathVariable UUID id, @RequestBody ReviewReportRequestDto dto) {
        return ResponseEntity.ok(reviewReportUseCase.execute(id, dto.getStatus()));
    }

    @GetMapping("/settings")
    public ResponseEntity<PlatformSettings> getSettings() {
        return ResponseEntity.ok(getPlatformSettingsUseCase.execute());
    }

    @PutMapping("/settings")
    public ResponseEntity<PlatformSettings> updateSettings(@RequestBody SettingsDto dto) {
        return ResponseEntity.ok(updatePlatformSettingsUseCase.execute(dto));
    }
}
