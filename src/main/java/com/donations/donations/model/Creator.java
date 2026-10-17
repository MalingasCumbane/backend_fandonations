package com.donations.donations.model;

import com.donations.donations.model.CreatorStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private LocalDateTime submittedAt;
}
