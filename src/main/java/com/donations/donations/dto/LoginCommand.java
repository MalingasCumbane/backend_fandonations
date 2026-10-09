package com.donations.donations.dto;

import lombok.Data;

@Data
public class LoginCommand {
    private String email;
    private String password;
}
