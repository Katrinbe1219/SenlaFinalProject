package org.example.core.dto.patching;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.annotations.NullOrNotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDefaultPatchDto {
    @NullOrNotBlank(message = "newUsername must be null or not blank")
    private String newUsername;
    @NullOrNotBlank(message = "newLogin must be null or not blank")
    private String newLogin;
    @NullOrNotBlank(message = "newEmail must be null or not blank")
    @Email(message = "your email is not valid")
    private String newEmail;
}
