package com.donations.donations.application.port.out;

import com.donations.donations.domain.model.Notification;
import java.util.Optional;
import java.util.UUID;

public interface NotificationPort {
    Notification save(Notification notification);
    Optional<Notification> findById(UUID id);
    java.util.List<Notification> findAll();
}
