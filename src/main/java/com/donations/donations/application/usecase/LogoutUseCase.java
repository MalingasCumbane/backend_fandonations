package com.donations.donations.application.usecase;

import com.donations.donations.infrastructure.security.JwtUtils;
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
