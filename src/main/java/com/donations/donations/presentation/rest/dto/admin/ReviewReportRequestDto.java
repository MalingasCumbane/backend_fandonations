package com.donations.donations.presentation.rest.dto.admin;

import com.donations.donations.domain.model.enums.ReportStatus;
import lombok.Data;

@Data
public class ReviewReportRequestDto {
    private ReportStatus status;
}
