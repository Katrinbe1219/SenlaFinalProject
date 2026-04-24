package org.example.application.dto.patching;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDefaultPatchDto {
    private String newUsername;
    private String newLogin;
    private String newEmail;
}
