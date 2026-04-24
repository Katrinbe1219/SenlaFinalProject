package org.example.application.dto.getting;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.application.dto.getting.goods.GoodIdDto;
import org.example.application.dto.getting.users.ModeratorSmallDto;
import org.example.application.models.types.ModeratorVerdict;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModeratorRecalcDto {
    private Long id;
    private ModeratorSmallDto moderator;
    private GoodIdDto good;
    private ModeratorVerdict verdict;
    private String comment;
    @JsonFormat(pattern = "dd.MM.yyyy", timezone = "Europe/Moscow")
    private Instant checkAt;

}
