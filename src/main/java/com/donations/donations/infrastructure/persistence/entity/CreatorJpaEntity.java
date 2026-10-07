package com.donations.donations.infrastructure.persistence.entity;

import com.donations.donations.domain.model.CreatorStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Table(name = "creators")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CreatorJpaEntity extends TimeStamp {

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
    private String country;
    private String city;
    private String category;
    
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

    @Column(columnDefinition = "TEXT")
    private String adminNote;
    
    @Column(columnDefinition = "TEXT")
    private String rejectionReason;
}
