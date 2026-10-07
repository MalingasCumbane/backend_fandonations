package com.donations.donations.application.dto;

import com.donations.donations.domain.model.UserRole;
import lombok.Data;

@Data
public class SignupCommand {
    private String name;
    private String email;
    private String password;
    private UserRole role;
}
