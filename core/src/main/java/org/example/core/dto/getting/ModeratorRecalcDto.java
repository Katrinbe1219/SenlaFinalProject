package org.example.core.dto.getting;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.core.dto.getting.goods.GoodIdDto;
import org.example.core.dto.getting.users.ModeratorSmallDto;
import org.example.core.models.types.ModeratorVerdict;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModeratorRecalcDto {
    @Positive(message = "id must be > 0")
    private Long id;
    private ModeratorSmallDto moderator;
    private GoodIdDto good;
    private ModeratorVerdict verdict;
    private String comment;
    @JsonFormat(pattern = "dd.MM.yyyy", timezone = "Europe/Moscow")
    private Instant checkAt;

}
