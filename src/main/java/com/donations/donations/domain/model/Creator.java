package com.donations.donations.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Creator {
    private UUID id;
    private UUID userId;
    private String fullName;
    private String username;
    private String email;
    private String phone;
    private String country;
    private String city;
    private String category;
    private String bio;
    private String avatarUrl;
    private CreatorStatus status;
    private String facebookUrl;
    private String twitterUrl;
    private String instagramUrl;
    private String youtubeUrl;
    private String tiktokUrl;
    private String adminNote;
    private String rejectionReason;
    private String requestedFullName;
    private LocalDateTime submittedAt;
    private LocalDateTime createdAt;
}
