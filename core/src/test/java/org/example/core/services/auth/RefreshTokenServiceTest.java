package org.example.core.services.auth;

import org.apache.logging.log4j.Logger;
import org.example.core.dto.getting.RefreshTokenDto;
import org.example.core.exceptions.InvalidTokenException;
import org.example.core.hibernate.base_settings.filters.RefreshTokenFilter;
import org.example.core.hibernate.documents.RefreshTokenHibImpl;
import org.example.core.mapping.RefreshTokenMapper;
import org.example.core.models.RefreshToken;
import org.example.core.models.User;
import org.example.core.security.JwtService;
import org.example.core.security.TokenPair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.format.annotation.DurationFormat;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceTest {

    @Mock
    RefreshTokenHibImpl refreshTokenHib;
    @InjectMocks
    RefreshTokenService refreshTokenService;

    @Mock
    RefreshTokenMapper mapper;

    @Mock
    JwtService jwtService;

    @Test
    @Tag("positive")
    @DisplayName("createTokenIfOldExists")
    void createTokenIfOldExists(){
        when(refreshTokenHib.findByUserAndDevice(anyLong(), anyString()))
                .thenReturn(Optional.of(new RefreshToken()));
        User  user = new User();
        user.setId(1L);
        refreshTokenService.createToken(user, "device");
        verify(refreshTokenHib).delete(any(), any(Logger.class));
    }

    @Test
    @Tag("positive")
    @DisplayName("createTokenIfOldDoesNotExist")
    void createTokenIfOldDoesNotExist(){
        when(refreshTokenHib.findByUserAndDevice(anyLong(), anyString()))
                .thenReturn(Optional.empty());
        User  user = new User();
        user.setId(1L);
        refreshTokenService.createToken(user, "device");
        verify(refreshTokenHib, never()).delete(any(), any(Logger.class));
    }

    @Test
    @Tag("negative")
    @DisplayName("rotateIFDoesNotExist")
    void rotateIFDoesNotExist(){
        when(refreshTokenHib.findFullByTokenHash(anyString())).thenReturn(Optional.empty());
        Assertions.assertThrows(InvalidTokenException.class, () ->
                refreshTokenService.rotate("raw", "device"));
    }

    @Test
    @Tag("negative")
    @DisplayName("rotateIfExpired")
    void rotateIfExpired(){
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setId(1L);
        refreshToken.setExpiresAt(Instant.now().minus(100, DurationFormat.Unit.HOURS.asChronoUnit()));

        when(refreshTokenHib.findFullByTokenHash(anyString()))
                .thenReturn(Optional.of(refreshToken));

        Assertions.assertThrows(InvalidTokenException.class, () ->
                refreshTokenService.rotate("raw", "device"));
        verify(refreshTokenHib).delete(any(), any(Logger.class));
    }

    @Test
    @Tag("positive")
    @DisplayName("rotateIfSuccessful")
    void rotateIfSuccessful(){
        User user = new User();
        user.setId(1L);
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setId(1L);
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(Instant.now().plus(1, DurationFormat.Unit.DAYS.asChronoUnit()));

        when(refreshTokenHib.findFullByTokenHash(anyString()))
                .thenReturn(Optional.of(refreshToken));
        when(jwtService.generateToken(any(User.class))).thenReturn("token");
        TokenPair res = refreshTokenService.rotate("raw", "device");

        Assertions.assertNotNull(res.accessToken());
        Assertions.assertNotNull(res.refreshToken());
        verify(refreshTokenHib).delete(eq(1L), any(Logger.class));
    }


    @Test
    @Tag("negative")
    @DisplayName("getTokensByUserIdIfDoesNotExist")
    void getTokensByUserIdIfDoesNotExist(){
        when(refreshTokenHib.getActiveSessionsByUser(anyLong()))
                .thenReturn(List.of());

        Assertions.assertNotNull(refreshTokenService.getTokensByUserId(1L));
    }

    @Test
    @Tag("positive")
    @DisplayName("getTokensByUserIdIfSuccessful")
    void getTokensByUserIdIfSuccessful(){
        when(refreshTokenHib.getActiveSessionsByUser(anyLong()))
                .thenReturn(List.of(new RefreshToken(), new RefreshToken()));

        when(mapper.toDto(any(RefreshToken.class))).thenReturn(new RefreshTokenDto());
        List<RefreshTokenDto> res = refreshTokenService.getTokensByUserId(1L);
        Assertions.assertEquals(2, res.size());
        verify(mapper, times(2)).toDto(any());
    }

    @Test
    @Tag("positive")
    @DisplayName("getAllByFiltersIfSuccessful")
    void getAllByFiltersIfSuccessful(){
        when(refreshTokenHib.findAllByFilters(any(RefreshTokenFilter.class)))
                .thenReturn(List.of(new RefreshToken(), new RefreshToken()));

        when(mapper.toDto(any(RefreshToken.class))).thenReturn(new RefreshTokenDto());
        List<RefreshTokenDto> res = refreshTokenService.getAllByFilters(new RefreshTokenFilter());
        Assertions.assertEquals(2, res.size());
        verify(mapper, times(2)).toDto(any());
    }

    @Test
    @Tag("negative")
    @DisplayName("getAllByFiltersIfDoesNotExist")
    void getAllByFiltersIfDoesNotExist(){
        when(refreshTokenHib.findAllByFilters(any(RefreshTokenFilter.class))).thenReturn(List.of());
        Assertions.assertNotNull(refreshTokenService.getAllByFilters(new RefreshTokenFilter()));
    }
}
