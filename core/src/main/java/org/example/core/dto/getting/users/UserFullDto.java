package org.example.core.dto.getting.users;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.core.dto.RoleDto;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserFullDto {
    private Long id;
    private String login;
    private String username;
    private String email;
    @JsonFormat(pattern = "dd.MM.yyyy HH:mm:ss", timezone = "Europe/Moscow")
    private Instant updatedAt;
    @JsonFormat(pattern = "dd.MM.yyyy HH:mm:ss", timezone = "Europe/Moscow")
    private Instant createdAt;
    private Boolean nonLocked;
    private RoleDto role;

}
