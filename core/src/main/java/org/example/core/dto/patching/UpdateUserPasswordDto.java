package org.example.core.dto.patching;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UpdateUserPasswordDto {
    @NotBlank
    private String oldPassword;
    @NotBlank
    private String newPassword;
}
