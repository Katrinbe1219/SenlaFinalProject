package org.example.application.dto.getting.users;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserForModeratorDto {
    private Long id;
    private String username;
    private Long roleId;
    private String roleName;
}
