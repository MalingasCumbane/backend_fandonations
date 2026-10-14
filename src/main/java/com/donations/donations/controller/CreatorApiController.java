package com.donations.donations.controller;

import com.donations.donations.service.creator.*;
import com.donations.donations.repository.CreatorRepository;
import com.donations.donations.model.Campaign;
import com.donations.donations.model.Creator;
import com.donations.donations.model.Donation;
import com.donations.donations.model.Payout;
import com.donations.donations.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CreatorApiController {

    private final CreatorRepository creatorRepository;
    private final GetCreatorCampaignsUseCase getCreatorCampaignsUseCase;
    private final CreateCampaignUseCase createCampaignUseCase;
    private final UpdateCampaignUseCase updateCampaignUseCase;
    private final DeleteCampaignUseCase deleteCampaignUseCase;
    private final GetCreatorDonationsUseCase getCreatorDonationsUseCase;
    private final GetCreatorStatsUseCase getCreatorStatsUseCase;
    private final GetCreatorPayoutsUseCase getCreatorPayoutsUseCase;
    private final RequestPayoutUseCase requestPayoutUseCase;
    private final com.donations.donations.service.wallet.WalletService walletService;
    private final com.donations.donations.service.security.SecurityOtpService securityOtpService;
    private final com.donations.donations.repository.EmailRepository emailRepository;
    private final com.donations.donations.repository.UserRepository userRepository;

    private UUID getCreatorId(UUID userId) {
        return creatorRepository.findByUserId(userId).orElseThrow(() -> new IllegalArgumentException("Criador não encontrado")).getId();
    }

    @GetMapping("/campaigns/me")
    public ResponseEntity<List<Campaign>> getCampaigns(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(getCreatorCampaignsUseCase.execute(getCreatorId(userDetails.getId())));
    }

    @PostMapping("/campaigns/me")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Campaign> createCampaign(@RequestBody Campaign campaign, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        campaign.setCreatorId(getCreatorId(userDetails.getId()));
        return ResponseEntity.ok(createCampaignUseCase.execute(campaign));
    }

    @PutMapping("/campaigns/me/{id}")
    public ResponseEntity<Campaign> updateCampaign(@PathVariable UUID id, @RequestBody Campaign campaign, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(updateCampaignUseCase.execute(id, campaign, getCreatorId(userDetails.getId())));
    }

    @DeleteMapping("/campaigns/me/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCampaign(@PathVariable UUID id, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        deleteCampaignUseCase.execute(id, getCreatorId(userDetails.getId()));
    }

    @GetMapping("/donations/me")
    public ResponseEntity<List<Donation>> getDonations(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(getCreatorDonationsUseCase.execute(getCreatorId(userDetails.getId())));
    }

    @GetMapping("/stats/me")
    public ResponseEntity<Map<String, Object>> getStats(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(getCreatorStatsUseCase.execute(getCreatorId(userDetails.getId())));
    }

    @GetMapping("/payouts/me")
    public ResponseEntity<List<Payout>> getPayouts(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(getCreatorPayoutsUseCase.execute(getCreatorId(userDetails.getId())));
    }

    @PostMapping("/payouts/me/send-otp")
    public ResponseEntity<?> sendPayoutOtp(@RequestBody com.donations.donations.dto.creator.PayoutRequestDto request, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        UUID creatorId = getCreatorId(userDetails.getId());
        var validation = requestPayoutUseCase.validatePayoutRequest(creatorId, request.getAmount(), request.getMethod(), request.getPhone());

        String targetInfo = "method=" + request.getMethod().name() + ";phone=" + validation.digits() + ";amount=" + request.getAmount();
        String otp = securityOtpService.generateAndSaveOtp(userDetails.getId(), "PAYOUT_REQUEST", targetInfo);

        com.donations.donations.model.User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new IllegalArgumentException("Utilizador não encontrado."));

        String amountFormatted = String.format("%,.2f", request.getAmount());
        emailRepository.sendPayoutOtpEmail(user.getEmail(), otp, amountFormatted, request.getMethod().name(), validation.digits());

        return ResponseEntity.ok(Map.of(
                "message", "Código de autorização enviado para o seu email.",
                "expiresInMinutes", 10
        ));
    }

    @PostMapping("/payouts/me")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Payout> requestPayout(@RequestBody com.donations.donations.dto.creator.PayoutRequestDto request, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        UUID creatorId = getCreatorId(userDetails.getId());
        if (request.getOtp() == null || request.getOtp().isBlank()) {
            throw new IllegalArgumentException("O código OTP é obrigatório para confirmar o levantamento.");
        }
        securityOtpService.verifyAndConsumeOtp(userDetails.getId(), "PAYOUT_REQUEST", request.getOtp());

        Payout saved = requestPayoutUseCase.execute(creatorId, request.getAmount(), request.getMethod(), request.getPhone());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/wallet/transactions/me")
    public ResponseEntity<List<com.donations.donations.dto.creator.WalletTransactionResponseDto>> getMyWalletTransactions(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        UUID creatorId = getCreatorId(userDetails.getId());
        return ResponseEntity.ok(walletService.getTransactionsForCreator(creatorId));
    }
}
