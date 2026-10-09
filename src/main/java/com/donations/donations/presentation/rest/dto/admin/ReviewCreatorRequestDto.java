package com.donations.donations.presentation.rest.dto.admin;

import com.donations.donations.domain.model.CreatorStatus;
import lombok.Data;

@Data
public class ReviewCreatorRequestDto {
    private CreatorStatus status;
    private String adminNote;
}
