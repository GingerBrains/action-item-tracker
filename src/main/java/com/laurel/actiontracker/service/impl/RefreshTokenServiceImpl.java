package com.laurel.actiontracker.service.impl;

import com.laurel.actiontracker.dto.internal.RefreshTokenResult;
import com.laurel.actiontracker.entity.RefreshToken;
import com.laurel.actiontracker.entity.User;
import com.laurel.actiontracker.exception.ResourceNotFoundException;
import com.laurel.actiontracker.exception.TokenExpiredException;
import com.laurel.actiontracker.repository.RefreshTokenRepository;
import com.laurel.actiontracker.security.TokenHashUtil;
import com.laurel.actiontracker.service.RefreshTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
@Transactional
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshExpirationMs;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenServiceImpl(RefreshTokenRepository refreshTokenRepository,
                                   @Value("${app.jwt.refresh-expiration-ms}") long refreshExpirationMs) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshExpirationMs = refreshExpirationMs;
    }


    @Override
    public RefreshTokenResult createRefreshToken(User user) {
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        String tokenHash = TokenHashUtil.hash(rawToken);
        Instant expiryDate = Instant.now().plusMillis(refreshExpirationMs);

        RefreshToken saved = refreshTokenRepository.save(new RefreshToken(tokenHash, user, expiryDate));
        return new RefreshTokenResult(rawToken, saved);
    }

    @Override
    public RefreshToken validateAndGetByRawToken(String rawToken) {
        return findRefreshTokenOrThrow(rawToken);
    }

    @Override
    public void deleteToken(RefreshToken token) {
        refreshTokenRepository.delete(token);
    }

    @Override
    public void deleteByRawToken(String rawToken) {
        refreshTokenRepository.deleteByTokenHash(TokenHashUtil.hash(rawToken));
    }

    @Override
    public void deleteExpiredAndRevokedTokens() {
        refreshTokenRepository.deleteByRevokedTrueOrExpiryDateBefore(Instant.now());
    }

    private RefreshToken findRefreshTokenOrThrow(String rawToken){
        String tokenHash = TokenHashUtil.hash(rawToken);

        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash).orElseThrow(() -> new ResourceNotFoundException("Token not found"));

        if (refreshToken.isRevoked()) {
            throw new TokenExpiredException("Refresh token has been revoked");
        }
        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            throw new TokenExpiredException("Refresh token has expired");
        }
        return refreshToken;
    }
}
