package com.inventory.management.service;

import com.inventory.management.domain.entity.RefreshToken;
import com.inventory.management.domain.entity.User;
import com.inventory.management.domain.repository.RefreshTokenRepository;
import com.inventory.management.domain.repository.UserRepository;
import com.inventory.management.dto.request.LoginRequest;
import com.inventory.management.dto.request.RefreshTokenRequest;
import com.inventory.management.dto.response.AuthResponse;
import com.inventory.management.exception.BusinessException;
import com.inventory.management.security.CustomUserDetails;
import com.inventory.management.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuditService auditService;

    @Value("${app.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Value("${app.jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        User user = userRepository.findByEmailAndActiveTrue(request.email())
                .orElseThrow(() -> new BusinessException("Inactive user", HttpStatus.FORBIDDEN));

        refreshTokenRepository.revokeAllUserTokens(user);

        String accessToken  = jwtService.generateAccessToken(userDetails);
        String refreshToken = createRefreshToken(user);

        auditService.logAsync(user, "LOGIN", "USER", String.valueOf(user.getId()),
                null, null, ipAddress);

        log.info("User {} logged in successfully", user.getEmail());

        return AuthResponse.of(accessToken, refreshToken, accessTokenExpiration,
                user.getId(), user.getEmail(), user.getFullName(), userDetails.getRole());
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new BusinessException("Invalid refresh token", HttpStatus.UNAUTHORIZED));

        if (!stored.isValid()) {
            throw new BusinessException("Refresh token expired or revoked", HttpStatus.UNAUTHORIZED);
        }

        User user = stored.getUser();
        CustomUserDetails userDetails = new CustomUserDetails(user);

        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        String newAccessToken  = jwtService.generateAccessToken(userDetails);
        String newRefreshToken = createRefreshToken(user);

        return AuthResponse.of(newAccessToken, newRefreshToken, accessTokenExpiration,
                user.getId(), user.getEmail(), user.getFullName(), userDetails.getRole());
    }

    @Transactional
    public void logout(String refreshTokenValue, User currentUser) {
        refreshTokenRepository.findByToken(refreshTokenValue).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
        auditService.logAsync(currentUser, "LOGOUT", "USER",
                String.valueOf(currentUser.getId()), null, null, null);
    }

    private String createRefreshToken(User user) {
        RefreshToken rt = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiresAt(Instant.now().plusMillis(refreshTokenExpiration))
                .build();
        return refreshTokenRepository.save(rt).getToken();
    }
}
