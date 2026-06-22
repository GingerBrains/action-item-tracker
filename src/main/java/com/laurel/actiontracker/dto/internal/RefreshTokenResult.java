package com.laurel.actiontracker.dto.internal;

import com.laurel.actiontracker.entity.RefreshToken;

public record RefreshTokenResult(String rawToken, RefreshToken refreshToken) {

}
