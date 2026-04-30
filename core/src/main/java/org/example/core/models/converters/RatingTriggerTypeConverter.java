package org.example.core.models.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.example.core.exceptions.NotCorrectInput;
import org.example.core.models.types.RatingTriggerType;

@Converter(autoApply = true)
public class RatingTriggerTypeConverter implements AttributeConverter<RatingTriggerType, String> {
    @Override
    public String convertToDatabaseColumn(RatingTriggerType ratingTriggerType) {
        return switch (ratingTriggerType){
            case MODERATOR -> "MODERATOR";
            case SCHEDULED -> "SCHEDULED";
            case ADMIN -> "ADMIN";

        };
    }

    @Override
    public RatingTriggerType convertToEntityAttribute(String s) {
        return switch (s.toUpperCase()){
            case "MODERATOR" -> RatingTriggerType.MODERATOR;
            case "SCHEDULED" -> RatingTriggerType.SCHEDULED;
            case "ADMIN" -> RatingTriggerType.ADMIN;

            default -> throw new NotCorrectInput("Unexpected value: " + s.toUpperCase());
        };
    }
}
