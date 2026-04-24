package org.example.core.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDto {
    @NotBlank
    private String username;
    @NotBlank
    private String login;
    private String email;
    @NotBlank
    private String password;
}
