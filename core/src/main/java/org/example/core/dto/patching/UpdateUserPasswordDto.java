package org.example.core.dto.patching;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UpdateUserPasswordDto {
    @NotBlank(message = "oldPassword can not by null")
    private String oldPassword;
    @NotBlank(message = "oldPassword can not by null")
    private String newPassword;
}
