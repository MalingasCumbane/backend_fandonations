package com.donations.donations.infrastructure.persistence.adapter;

import com.donations.donations.application.port.out.NotificationPort;
import com.donations.donations.domain.model.Notification;
import com.donations.donations.infrastructure.persistence.entity.NotificationJpaEntity;
import com.donations.donations.infrastructure.persistence.mapper.NotificationMapper;
import com.donations.donations.infrastructure.persistence.repository.NotificationJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class NotificationPersistenceAdapter implements NotificationPort {

    private final NotificationJpaRepository notificationRepository;

    @Override
    public Notification save(Notification notification) {
        NotificationJpaEntity entity = NotificationMapper.toEntity(notification);
        NotificationJpaEntity savedEntity = notificationRepository.save(entity);
        return NotificationMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Notification> findById(UUID id) {
        return notificationRepository.findById(id).map(NotificationMapper::toDomain);
    }

    @Override
    public List<Notification> findAll() {
        return notificationRepository.findAll().stream().map(NotificationMapper::toDomain).collect(Collectors.toList());
    }
}
