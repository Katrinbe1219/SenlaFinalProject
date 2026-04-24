package org.example.core.services.auth;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
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
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
// TODO revoke All By user_id after changing password
@AllArgsConstructor
public class RefreshTokenService {
    private static final Logger logger = LogManager.getLogger(RefreshTokenService.class);
    private RefreshTokenMapper mapper;

    private RefreshTokenHibImpl repo;
    private JwtService jwtService;
    private final Duration TTL = Duration.ofDays(7);


    @Transactional
    public String createToken(User user, String deviceInfo){
        Optional<RefreshToken> old = repo.findByUserAndDevice(user.getId(), deviceInfo);
        old.ifPresent(refreshToken -> repo.delete(refreshToken.getId(), logger));


        String raw = UUID.randomUUID().toString() + UUID.randomUUID().toString();
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setDeviceInfo(deviceInfo);
        token.setCreatedAt(Instant.now());
        token.setLastUsedAt(Instant.now());
        token.setExpiresAt(Instant.now().plus(TTL));
        token.setTokenHash(hash(raw));
        repo.save(token, logger);
        return raw;
    }

    @Transactional
    public TokenPair rotate(String raw, String deviceInfo){
        Optional<RefreshToken> stored = repo.findFullByTokenHash(hash(raw));
        if (stored.isEmpty()){
            throw new InvalidTokenException("Refresh token not found");
        }

        if (stored.get().getExpiresAt().isBefore(Instant.now())){
            repo.delete(stored.get().getId(), logger);
            throw new InvalidTokenException("Refresh token expired");
        }

        User user = stored.get().getUser();
        repo.delete(stored.get().getId(), logger);
        String newRefresh = createToken(user, deviceInfo);
        String newAccess = jwtService.generateToken(user);

        return new TokenPair(newRefresh, newAccess);
    }

    @Transactional
    public void revokeByUserId(Long userId){
        repo.deleteAllByUser(userId);
    }

    @Transactional
    public List<RefreshTokenDto> getTokensByUserId(Long userId){
        List<RefreshToken> tokens = repo.getActiveSessionsByUser(userId);

        if (tokens.isEmpty()) return List.of();

        List<RefreshTokenDto> dtos = new ArrayList<>();
        for (RefreshToken token : tokens){
            dtos.add(mapper.toDto(token));
        }
        return dtos;
    }

    @Transactional
    public List<RefreshTokenDto> getAllByFilters(RefreshTokenFilter filters){
        List<RefreshToken> tokens = repo.findAllByFilters(filters);
        if (tokens.isEmpty()) return List.of();
        List<RefreshTokenDto> dtos = new ArrayList<>();
        for (RefreshToken token : tokens){
            dtos.add(mapper.toDto(token));
        }

        return dtos;
    }



    private String hash(String raw){
        return DigestUtils.sha256Hex(raw);
    }



}
