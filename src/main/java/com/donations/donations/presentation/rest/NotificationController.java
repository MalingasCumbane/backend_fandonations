package com.donations.donations.presentation.rest;

import com.donations.donations.application.port.out.NotificationPort;
import com.donations.donations.domain.model.Notification;
import com.donations.donations.infrastructure.security.UserDetailsImpl;
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

    private final NotificationPort notificationPort;

    @GetMapping("/me")
    public ResponseEntity<List<Notification>> getMine(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        // Find all notifications matching userId
        List<Notification> all = notificationPort.findAll().stream()
            .filter(n -> n.getUserId().equals(userDetails.getId()))
            .collect(Collectors.toList());
        return ResponseEntity.ok(all);
    }

    @PutMapping("/me/read-all")
    public ResponseEntity<Void> readAll(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        notificationPort.findAll().stream()
            .filter(n -> n.getUserId().equals(userDetails.getId()))
            .forEach(n -> {
                n.setRead(true);
                notificationPort.save(n);
            });
        return ResponseEntity.ok().build();
    }
}