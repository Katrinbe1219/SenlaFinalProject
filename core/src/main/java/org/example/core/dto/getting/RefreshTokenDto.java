package org.example.core.dto.getting;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.core.dto.getting.users.ModeratorSmallDto;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenDto {
    private Long id;
    private String tokenHash;
    private String deviceInfo;
    @JsonFormat(pattern = "dd.MM.yyyy HH:mm:ss", timezone = "Europe/Moscow")
    private Instant expiresAt;
    @JsonFormat(pattern = "dd.MM.yyyy HH:mm:ss", timezone = "Europe/Moscow")
    private Instant lastUsedAt;
    private ModeratorSmallDto user;
}
