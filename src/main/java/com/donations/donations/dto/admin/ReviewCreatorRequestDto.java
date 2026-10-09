package com.donations.donations.dto.admin;

import com.donations.donations.model.CreatorStatus;
import lombok.Data;

@Data
public class ReviewCreatorRequestDto {
    private CreatorStatus status;
    private String adminNote;
}
