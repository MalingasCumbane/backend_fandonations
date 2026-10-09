package com.donations.donations.dto;

import com.donations.donations.model.UserRole;
import lombok.Data;

@Data
public class SignupCommand {
    private String name;
    private String email;
    private String password;
    private UserRole role;
}
