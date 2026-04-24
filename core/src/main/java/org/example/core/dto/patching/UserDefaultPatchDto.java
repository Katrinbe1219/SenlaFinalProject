package org.example.core.dto.patching;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.annotations.NullOrNotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDefaultPatchDto {
    @NullOrNotBlank
    private String newUsername;
    @NullOrNotBlank
    private String newLogin;
    @NullOrNotBlank
    private String newEmail;
}
