package com.donations.donations.controller;

import com.donations.donations.repository.NotificationRepository;
import com.donations.donations.model.Notification;
import com.donations.donations.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;

    @GetMapping("/me")
    public ResponseEntity<com.donations.donations.dto.PageResponse<Notification>> getMine(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(Math.max(0, page), Math.max(1, size));
        return ResponseEntity.ok(com.donations.donations.dto.PageResponse.of(notificationRepository.findByUserIdOrderByCreatedAtDesc(userDetails.getId(), pageable)));
    }

    @PutMapping("/me/read-all")
    public ResponseEntity<Void> readAll(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        notificationRepository.findAll().stream()
            .filter(n -> n.getUserId().equals(userDetails.getId()))
            .forEach(n -> {
                n.setRead(true);
                notificationRepository.save(n);
            });
        return ResponseEntity.ok().build();
    }
}