package com.donations.donations.dto.admin;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class AdminStatsResponseDto {
    private PlatformStats stats;
    private List<SeriesPoint> perDay;
    private List<SeriesPoint> perMonth;
    private List<MethodShare> byMethod;

    @Data
    @Builder
    public static class PlatformStats {
        private long creators;
        private long verified;
        private long pending;
        private long donations;
        private BigDecimal volume;
        private BigDecimal revenue;
        private long failed;
        private BigDecimal feePercentage;
    }

    @Data
    @Builder
    public static class SeriesPoint {
        private String label;
        private BigDecimal value;
    }

    @Data
    @Builder
    public static class MethodShare {
        private String method;
        private BigDecimal value;
        private BigDecimal share;
    }
}
