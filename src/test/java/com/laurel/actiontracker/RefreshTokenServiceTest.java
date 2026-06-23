package com.laurel.actiontracker;

import com.laurel.actiontracker.dto.internal.RefreshTokenResult;
import com.laurel.actiontracker.entity.RefreshToken;
import com.laurel.actiontracker.entity.User;
import com.laurel.actiontracker.exception.ResourceNotFoundException;
import com.laurel.actiontracker.exception.TokenExpiredException;
import com.laurel.actiontracker.repository.RefreshTokenRepository;
import com.laurel.actiontracker.security.TokenHashUtil;
import com.laurel.actiontracker.service.impl.RefreshTokenServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private RefreshTokenServiceImpl refreshTokenService;

    private static final long EXPIRATION_MS = 604800000L;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenServiceImpl(refreshTokenRepository, EXPIRATION_MS);
    }

    // --- createRefreshToken ---

    @Test
    void createRefreshToken_returnsRawTokenAndSavesHashedToken() {
        User user = new User();
        user.setId(1L);

        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RefreshTokenResult result = refreshTokenService.createRefreshToken(user);

        assertThat(result.rawToken()).isNotNull();
        assertThat(result.refreshToken()).isNotNull();

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository, times(1)).save(captor.capture());

        RefreshToken saved = captor.getValue();
        assertThat(saved.getTokenHash()).isEqualTo(TokenHashUtil.hash(result.rawToken()));
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getExpiryDate()).isAfter(Instant.now());
    }

    @Test
    void createRefreshToken_setsExpiryRelativeToNow() {
        User user = new User();
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Instant before = Instant.now();
        refreshTokenService.createRefreshToken(user);
        Instant after = Instant.now();

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());

        Instant expiry = captor.getValue().getExpiryDate();
        assertThat(expiry.isAfter(before.plusMillis(EXPIRATION_MS - 1000))).isTrue();
        assertThat(expiry.isBefore(after.plusMillis(EXPIRATION_MS + 1000))).isTrue();
    }

    // --- validateAndGetByRawToken ---

    @Test
    void validateAndGetByRawToken_returnsTokenWhenValid() {
        String rawToken = "validRawToken";
        String hash = TokenHashUtil.hash(rawToken);
        User user = new User();
        RefreshToken token = new RefreshToken(hash, user, Instant.now().plusSeconds(3600));

        when(refreshTokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(token));

        RefreshToken result = refreshTokenService.validateAndGetByRawToken(rawToken);

        assertThat(result).isEqualTo(token);
    }

    @Test
    void validateAndGetByRawToken_throwsResourceNotFoundWhenTokenMissing() {
        String rawToken = "unknownToken";
        when(refreshTokenRepository.findByTokenHash(TokenHashUtil.hash(rawToken)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.validateAndGetByRawToken(rawToken))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Token not found");
    }

    @Test
    void validateAndGetByRawToken_throwsTokenExpiredWhenRevoked() {
        String rawToken = "revokedToken";
        String hash = TokenHashUtil.hash(rawToken);
        User user = new User();
        RefreshToken token = new RefreshToken(hash, user, Instant.now().plusSeconds(3600));
        token.setRevoked(true);

        when(refreshTokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> refreshTokenService.validateAndGetByRawToken(rawToken))
                .isInstanceOf(TokenExpiredException.class)
                .hasMessageContaining("revoked");
    }

    @Test
    void validateAndGetByRawToken_throwsTokenExpiredWhenExpired() {
        String rawToken = "expiredToken";
        String hash = TokenHashUtil.hash(rawToken);
        User user = new User();
        RefreshToken token = new RefreshToken(hash, user, Instant.now().minusSeconds(1));

        when(refreshTokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> refreshTokenService.validateAndGetByRawToken(rawToken))
                .isInstanceOf(TokenExpiredException.class)
                .hasMessageContaining("expired");
    }

    // --- deleteToken ---

    @Test
    void deleteToken_delegatesToRepository() {
        User user = new User();
        RefreshToken token = new RefreshToken("hash", user, Instant.now().plusSeconds(3600));

        refreshTokenService.deleteToken(token);

        verify(refreshTokenRepository, times(1)).delete(token);
    }

    // --- deleteByRawToken ---

    @Test
    void deleteByRawToken_hashesTokenBeforeDelegating() {
        String rawToken = "someRawToken";

        refreshTokenService.deleteByRawToken(rawToken);

        verify(refreshTokenRepository, times(1)).deleteByTokenHash(TokenHashUtil.hash(rawToken));
    }

    // --- deleteExpiredAndRevokedTokens ---

    @Test
    void deleteExpiredAndRevokedTokens_delegatesToRepository() {
        refreshTokenService.deleteExpiredAndRevokedTokens();

        verify(refreshTokenRepository, times(1)).deleteByRevokedTrueOrExpiryDateBefore(any(Instant.class));
    }
}
