package org.example.application.dto.patching;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UpdateUserPasswordDto {
    private String oldPassword;
    private String newPassword;
}
