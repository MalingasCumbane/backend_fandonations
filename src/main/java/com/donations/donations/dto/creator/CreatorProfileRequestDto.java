package com.donations.donations.dto.creator;

import lombok.Data;

@Data
public class CreatorProfileRequestDto {
    private String fullName;
    private String username;
    private String email;
    private String country;
    private String city;
    private String category;
    private String bio;
    private String avatarUrl;
    private String facebookUrl;
    private String twitterUrl;
    private String instagramUrl;
    private String youtubeUrl;
    private String tiktokUrl;
    private Boolean acceptsMarketing;
}
