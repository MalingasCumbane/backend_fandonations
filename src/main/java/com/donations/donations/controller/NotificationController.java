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
    public ResponseEntity<List<Notification>> getMine(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        // Find all notifications matching userId
        List<Notification> all = notificationRepository.findAll().stream()
            .filter(n -> n.getUserId().equals(userDetails.getId()))
            .collect(Collectors.toList());
        return ResponseEntity.ok(all);
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