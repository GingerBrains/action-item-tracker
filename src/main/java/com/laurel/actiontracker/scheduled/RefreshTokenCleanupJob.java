package com.laurel.actiontracker.scheduled;

import com.laurel.actiontracker.service.RefreshTokenService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenCleanupJob {

    private final RefreshTokenService refreshTokenService;

    public RefreshTokenCleanupJob(RefreshTokenService refreshTokenService) {
        this.refreshTokenService = refreshTokenService;
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void deleteExpiredAndRevokedTokens() {
        refreshTokenService.deleteExpiredAndRevokedTokens();
    }
}