package com.donations.donations.service;

import com.donations.donations.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogoutUseCase {

    private final JwtUtils jwtUtils;

    public ResponseCookie execute() {
        return jwtUtils.getCleanJwtCookie();
    }
}
