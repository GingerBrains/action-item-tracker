package com.laurel.actiontracker.service;

import com.laurel.actiontracker.dto.internal.RefreshTokenResult;
import com.laurel.actiontracker.entity.RefreshToken;
import com.laurel.actiontracker.entity.User;

public interface RefreshTokenService {
    RefreshTokenResult createRefreshToken(User user);
    RefreshToken validateAndGetByRawToken(String rawToken);
    void deleteToken(RefreshToken token);
    void deleteExpiredAndRevokedTokens();
}
