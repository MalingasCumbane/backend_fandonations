package com.donations.donations.controller.admin;

import com.donations.donations.service.admin.*;
import com.donations.donations.service.wallet.WalletService;
import com.donations.donations.model.*;
import com.donations.donations.dto.admin.*;
import com.donations.donations.dto.creator.WalletTransactionResponseDto;
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
    private final RejectFullNameUseCase rejectFullNameUseCase;
    private final GetVerificationsUseCase getVerificationsUseCase;
    private final GetDonationsUseCase getDonationsUseCase;
    private final GetPayoutsUseCase getPayoutsUseCase;
    private final ReviewPayoutUseCase reviewPayoutUseCase;
    private final GetReportsUseCase getReportsUseCase;
    private final ReviewReportUseCase reviewReportUseCase;
    private final GetPlatformSettingsUseCase getPlatformSettingsUseCase;
    private final UpdatePlatformSettingsUseCase updatePlatformSettingsUseCase;
    private final WalletService walletService;

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponseDto> getStats() {
        return ResponseEntity.ok(getAdminStatsUseCase.execute());
    }

    @GetMapping("/creators")
    public ResponseEntity<com.donations.donations.dto.PageResponse<java.util.Map<String, Object>>> getCreators(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(getCreatorsUseCase.executePaged(page, size));
    }

    @PostMapping("/creators/{id}/review")
    public ResponseEntity<Creator> reviewCreator(@PathVariable UUID id, @RequestBody ReviewCreatorRequestDto dto) {
        return ResponseEntity.ok(reviewCreatorUseCase.execute(id, dto.getStatus(), dto.getAdminNote()));
    }

    @PostMapping("/creators/{id}/approve-full-name")
    public ResponseEntity<Creator> approveFullName(@PathVariable UUID id) {
        return ResponseEntity.ok(approveFullNameUseCase.execute(id));
    }

    @PostMapping("/creators/{id}/reject-full-name")
    public ResponseEntity<Creator> rejectFullName(@PathVariable UUID id, @RequestBody(required = false) java.util.Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        return ResponseEntity.ok(rejectFullNameUseCase.execute(id, reason));
    }

    @GetMapping("/verifications")
    public ResponseEntity<com.donations.donations.dto.PageResponse<VerificationRequest>> getVerifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(getVerificationsUseCase.executePaged(page, size));
    }

    @GetMapping("/donations")
    public ResponseEntity<com.donations.donations.dto.PageResponse<Donation>> getDonations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(getDonationsUseCase.executePaged(page, size));
    }

    @GetMapping("/payouts")
    public ResponseEntity<com.donations.donations.dto.PageResponse<Payout>> getPayouts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(getPayoutsUseCase.executePaged(page, size));
    }

    @PostMapping("/payouts/{id}/status")
    public ResponseEntity<Payout> reviewPayout(@PathVariable UUID id, @RequestBody ReviewPayoutRequestDto dto) {
        return ResponseEntity.ok(reviewPayoutUseCase.execute(id, dto.getStatus(), dto.getAdminNote(), dto.getTransactionReference()));
    }

    @GetMapping("/reports")
    public ResponseEntity<com.donations.donations.dto.PageResponse<Report>> getReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(getReportsUseCase.executePaged(page, size));
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

    @GetMapping("/wallet/transactions")
    public ResponseEntity<com.donations.donations.dto.PageResponse<WalletTransactionResponseDto>> getWalletTransactions(
            @RequestParam(required = false) UUID creatorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String period
    ) {
        return ResponseEntity.ok(walletService.getAllTransactionsPaged(creatorId, page, size, type, category, search, period));
    }
}
