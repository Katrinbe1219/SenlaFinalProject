package org.example.application.models.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.example.application.models.types.RatingStatus;

@Converter(autoApply = true)
public class RatingStatusConverter implements AttributeConverter<RatingStatus, String> {
    @Override
    public String convertToDatabaseColumn(RatingStatus ratingStatus) {
        return switch (ratingStatus){
            case FAILED -> "FAILED";
            case SUCCESS -> "SUCCESS";
            case FIRST_ADDED -> "FIRST_ADDED";
        };
    }

    @Override
    public RatingStatus convertToEntityAttribute(String s) {
        return switch (s){
            case "FAILED" -> RatingStatus.FAILED;
            case "SUCCESS" -> RatingStatus.SUCCESS;
            case "FIRST_ADDED" -> RatingStatus.FIRST_ADDED;
            default -> RatingStatus.SUCCESS;
        };
    }
}
