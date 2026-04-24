package org.example.core.dto.getting;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileDto {
    private String username;
    private String login;
    private String role;
    @JsonFormat(pattern = "dd.MM.yyyy",timezone = "Europe/Moscow")
    private Instant createdAt;
    @JsonFormat(pattern = "dd.MM.yyyy",timezone = "Europe/Moscow")
    private Instant updatedAt;
}
