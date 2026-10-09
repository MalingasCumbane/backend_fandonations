package com.donations.donations.dto.admin;

import com.donations.donations.model.enums.ReportStatus;
import lombok.Data;

@Data
public class ReviewReportRequestDto {
    private ReportStatus status;
}
