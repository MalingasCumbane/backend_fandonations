package com.donations.donations.presentation.rest;

import com.donations.donations.application.usecase.creator.*;
import com.donations.donations.application.port.out.CreatorPort;
import com.donations.donations.domain.model.Campaign;
import com.donations.donations.domain.model.Creator;
import com.donations.donations.domain.model.Donation;
import com.donations.donations.domain.model.Payout;
import com.donations.donations.infrastructure.security.UserDetailsImpl;
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

    private final CreatorPort creatorPort;
    private final GetCreatorCampaignsUseCase getCreatorCampaignsUseCase;
    private final CreateCampaignUseCase createCampaignUseCase;
    private final UpdateCampaignUseCase updateCampaignUseCase;
    private final DeleteCampaignUseCase deleteCampaignUseCase;
    private final GetCreatorDonationsUseCase getCreatorDonationsUseCase;
    private final GetCreatorStatsUseCase getCreatorStatsUseCase;
    private final GetCreatorPayoutsUseCase getCreatorPayoutsUseCase;
    private final RequestPayoutUseCase requestPayoutUseCase;

    private UUID getCreatorId(UUID userId) {
        return creatorPort.findByUserId(userId).orElseThrow(() -> new IllegalArgumentException("Criador não encontrado")).getId();
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
        return ResponseEntity.ok(updateCampaignUseCase.execute(id, campaign));
    }

    @DeleteMapping("/campaigns/me/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCampaign(@PathVariable UUID id, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        deleteCampaignUseCase.execute(id);
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

    @PostMapping("/payouts/me")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Payout> requestPayout(@RequestBody Payout payout, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        UUID creatorId = getCreatorId(userDetails.getId());
        payout.setCreatorId(creatorId);
        return ResponseEntity.ok(requestPayoutUseCase.execute(payout.getCreatorId(), payout.getAmount(), payout.getMethod(), payout.getPhone()));
    }
    
    @PostMapping("/creators/me/avatar")
    public ResponseEntity<Void> uploadAvatar(@RequestBody Map<String, String> body, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        String avatar = body.get("avatar");
        if (avatar != null) {
            UUID creatorId = getCreatorId(userDetails.getId());
            Creator c = creatorPort.findById(creatorId).orElseThrow();
            c.setAvatarUrl(avatar);
            creatorPort.save(c);
        }
        return ResponseEntity.ok().build();
    }
}
