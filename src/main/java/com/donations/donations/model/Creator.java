package com.donations.donations.model;

import com.donations.donations.model.CreatorStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "creators")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Creator extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    private String fullName;

    @Column(unique = true)
    private String username;

    private String email;
    private String phone;

    @Column(name = "mpesa_phone")
    private String mpesaPhone;

    @Column(name = "emola_phone")
    private String emolaPhone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private Country country;
    private String city;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CreatorStatus status;

    private String facebookUrl;
    private String twitterUrl;
    private String instagramUrl;
    private String youtubeUrl;
    private String tiktokUrl;

    @Builder.Default
    private boolean acceptsMarketing = false;

    @Column(columnDefinition = "TEXT")
    private String adminNote;

    @Column(columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    private String requestedFullName;
    private String requestedUsername;
    @Column(columnDefinition = "TEXT")
    private String nameChangeReason;
    private LocalDateTime submittedAt;

    @Transient
    @JsonProperty("socials")
    public Map<String, String> getSocials() {
        Map<String, String> map = new HashMap<>();
        map.put("facebook", facebookUrl != null ? facebookUrl : "");
        map.put("twitter", twitterUrl != null ? twitterUrl : "");
        map.put("instagram", instagramUrl != null ? instagramUrl : "");
        map.put("youtube", youtubeUrl != null ? youtubeUrl : "");
        map.put("tiktok", tiktokUrl != null ? tiktokUrl : "");
        return map;
    }

    @Transient
    @JsonProperty("socials")
    public void setSocials(Map<String, String> socials) {
        if (socials != null) {
            if (socials.containsKey("facebook")) this.facebookUrl = socials.get("facebook");
            if (socials.containsKey("twitter")) this.twitterUrl = socials.get("twitter");
            if (socials.containsKey("instagram")) this.instagramUrl = socials.get("instagram");
            if (socials.containsKey("youtube")) this.youtubeUrl = socials.get("youtube");
            if (socials.containsKey("tiktok")) this.tiktokUrl = socials.get("tiktok");
        }
    }
}
