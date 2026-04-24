package org.example.core.dto.getting.reviews;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.core.dto.getting.users.UserForReviewDto;
import org.example.core.dto.getting.goods.GoodForReviewDto;


import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewFullDto {
    private Long id;
    private GoodForReviewDto good;
    private UserForReviewDto user;
    private String review;
    private Integer rate;
    @JsonFormat(pattern = "dd.MM.yyyy HH:mm:ss", timezone = "Europe/Moscow")
    private Instant createdAt;
    private Boolean blocked;
    @JsonFormat(pattern = "dd.MM.yyyy HH:mm:ss", timezone = "Europe/Moscow")
    private Instant blockedAt;
    private UserForReviewDto blockedBy;
}
