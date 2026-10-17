package com.donations.donations.service;

import com.donations.donations.dto.AuthCookies;
import com.donations.donations.dto.LoginCommand;
import com.donations.donations.model.UserSession;
import com.donations.donations.security.JwtUtils;
import com.donations.donations.security.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginUseCase {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserSessionService userSessionService;

    public AuthCookies execute(LoginCommand command, HttpServletRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(command.getEmail(), command.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Cria sessão persistente para o dispositivo
        UserSession session = userSessionService.createSession(userDetails.getId(), request);

        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails, session.getId());
        ResponseCookie refreshCookie = jwtUtils.generateRefreshTokenCookie(session.getRefreshToken());

        return new AuthCookies(jwtCookie, refreshCookie);
    }

    public ResponseCookie execute(LoginCommand command) {
        return execute(command, null).jwtCookie();
    }
}
