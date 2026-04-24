package org.example.application.dto.creating;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.application.models.Good;
import org.example.application.models.User;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewCreateDto {
    private String review;
    private int rate;
}
