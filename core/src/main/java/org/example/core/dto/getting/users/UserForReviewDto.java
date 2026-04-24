package org.example.core.dto.getting.users;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserForReviewDto {
    private Long id;
    private String username;
    private String role;
    private Boolean blocked;
}
