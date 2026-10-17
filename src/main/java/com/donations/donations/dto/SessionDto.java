package com.donations.donations.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionDto {
    private String id;
    private String deviceName;
    private String ipAddress;
    private LocalDateTime lastActiveAt;
    private LocalDateTime createdAt;
    private boolean isCurrent;
}
