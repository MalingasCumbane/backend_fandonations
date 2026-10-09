package com.donations.donations.dto.publics;

import com.donations.donations.model.CreatorStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PublicCreator {
    private UUID id;
    private UUID userId;
    private String username;
    private String fullName;
    private String country;
    private String city;
    private String category;
    private String bio;
    private String avatarUrl;
    private CreatorStatus status;
    private Socials socials;
    private LocalDateTime createdAt;

    @Data
    @Builder
    public static class Socials {
        private String instagram;
        private String tiktok;
        private String youtube;
        private String facebook;
        private String twitter;
    }
}
