package com.donations.donations.service.admin;

import com.donations.donations.repository.CreatorRepository;
import com.donations.donations.model.Creator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetCreatorsUseCase {
    private final CreatorRepository creatorRepository;

    public List<Map<String, Object>> execute() {
        return creatorRepository.findAll().stream().map(this::toMap).collect(Collectors.toList());
    }

    public com.donations.donations.dto.PageResponse<Map<String, Object>> executePaged(int page, int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(Math.max(0, page), Math.max(1, size));
        return com.donations.donations.dto.PageResponse.of(creatorRepository.findAllByOrderByCreatedAtDesc(pageable), this::toMap);
    }

    public Map<String, Object> toMap(Creator c) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", c.getId() != null ? c.getId().toString() : "");
        map.put("userId", c.getUserId() != null ? c.getUserId().toString() : "");
        map.put("fullName", c.getFullName() != null ? c.getFullName() : "");
        map.put("username", c.getUsername() != null ? c.getUsername() : "");
        map.put("email", c.getEmail() != null ? c.getEmail() : "");
        map.put("phone", c.getPhone() != null ? c.getPhone() : "");
        map.put("mpesaPhone", c.getMpesaPhone() != null ? c.getMpesaPhone() : "");
        map.put("emolaPhone", c.getEmolaPhone() != null ? c.getEmolaPhone() : "");
        map.put("city", c.getCity() != null ? c.getCity() : "");
        map.put("country", c.getCountry() != null ? c.getCountry().getName() : "");
        map.put("category", c.getCategory() != null ? c.getCategory().getName() : "");
        map.put("bio", c.getBio() != null ? c.getBio() : "");
        map.put("avatarUrl", c.getAvatarUrl());
        map.put("status", c.getStatus() != null ? c.getStatus().name() : "DRAFT");
        map.put("acceptsMarketing", c.isAcceptsMarketing());
        map.put("adminNote", c.getAdminNote());
        map.put("rejectionReason", c.getRejectionReason());
        map.put("requestedFullName", c.getRequestedFullName());
        map.put("submittedAt", c.getSubmittedAt() != null ? c.getSubmittedAt().toString() : null);
        map.put("createdAt", c.getCreatedAt() != null ? c.getCreatedAt().toString() : null);

        Map<String, String> socials = new HashMap<>();
        socials.put("facebook", c.getFacebookUrl());
        socials.put("twitter", c.getTwitterUrl());
        socials.put("instagram", c.getInstagramUrl());
        socials.put("youtube", c.getYoutubeUrl());
        socials.put("tiktok", c.getTiktokUrl());
        map.put("socials", socials);

        return map;
    }
}
