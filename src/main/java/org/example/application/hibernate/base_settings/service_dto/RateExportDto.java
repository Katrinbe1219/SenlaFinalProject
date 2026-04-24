package org.example.application.hibernate.base_settings.service_dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.application.models.types.RatingStatus;
import org.example.application.models.types.RatingTriggerType;

import java.time.Instant;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RateExportDto {

        private Long id;
        private Long goodId;
        private String goodName;
        private Long categoryId;
        private String categoryName;
        @JsonFormat(pattern = "dd.MM.yyyy HH:mm:ss", timezone = "Europe/Moscow")
        private Instant recalculatedAt;
        private RatingTriggerType triggeredBy;
        private RatingStatus ratingStatus;
        private String errorMessage;
        private Double rate;

}
