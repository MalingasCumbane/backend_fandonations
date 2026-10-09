package com.donations.donations.infrastructure.persistence.mapper;

import com.donations.donations.domain.model.Notification;
import com.donations.donations.infrastructure.persistence.entity.NotificationJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public static Notification toDomain(NotificationJpaEntity entity) {
        if (entity == null) return null;
        return Notification.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .title(entity.getTitle())
                .body(entity.getBody())
                .isRead(entity.isRead())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static NotificationJpaEntity toEntity(Notification domain) {
        if (domain == null) return null;
        NotificationJpaEntity entity = NotificationJpaEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .title(domain.getTitle())
                .body(domain.getBody())
                .isRead(domain.isRead())
                .build();

        // Initialize Base fields if new
        if (domain.getId() == null) {
            entity.setIsActive(true);
            entity.setIsDeleted(false);
            entity.setCreatedBy("system");
            entity.setCreatedIp("127.0.0.1");
            entity.setReference(java.util.UUID.randomUUID().toString().substring(0, 8));
        }

        return entity;
    }
}
