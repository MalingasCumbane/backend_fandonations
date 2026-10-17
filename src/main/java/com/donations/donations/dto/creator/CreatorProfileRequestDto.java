package com.donations.donations.dto.creator;

import lombok.Data;

@Data
public class CreatorProfileRequestDto {
    private String fullName;
    private java.time.LocalDate birthDate;
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
    private java.util.Map<String, String> socials;

    public String getFacebookUrl() {
        if (facebookUrl != null) return facebookUrl.trim();
        if (socials != null && socials.containsKey("facebook")) {
            String val = socials.get("facebook");
            return val != null ? val.trim() : null;
        }
        return null;
    }

    public String getTwitterUrl() {
        if (twitterUrl != null) return twitterUrl.trim();
        if (socials != null && socials.containsKey("twitter")) {
            String val = socials.get("twitter");
            return val != null ? val.trim() : null;
        }
        return null;
    }

    public String getInstagramUrl() {
        if (instagramUrl != null) return instagramUrl.trim();
        if (socials != null && socials.containsKey("instagram")) {
            String val = socials.get("instagram");
            return val != null ? val.trim() : null;
        }
        return null;
    }

    public String getYoutubeUrl() {
        if (youtubeUrl != null) return youtubeUrl.trim();
        if (socials != null && socials.containsKey("youtube")) {
            String val = socials.get("youtube");
            return val != null ? val.trim() : null;
        }
        return null;
    }

    public String getTiktokUrl() {
        if (tiktokUrl != null) return tiktokUrl.trim();
        if (socials != null && socials.containsKey("tiktok")) {
            String val = socials.get("tiktok");
            return val != null ? val.trim() : null;
        }
        return null;
    }
}
