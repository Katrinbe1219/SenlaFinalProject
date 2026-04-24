package org.example.application.models.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.example.application.exceptions.NotCorrectInput;
import org.example.application.models.types.RatingTriggerType;

@Converter(autoApply = true)
public class RatingTriggerTypeConverter implements AttributeConverter<RatingTriggerType, String> {
    @Override
    public String convertToDatabaseColumn(RatingTriggerType ratingTriggerType) {
        return switch (ratingTriggerType){
            case MODERATOR -> "MODERATOR";
            case SCHEDULED -> "SCHEDULED";

        };
    }

    @Override
    public RatingTriggerType convertToEntityAttribute(String s) {
        return switch (s.toUpperCase()){
            case "MODERATOR" -> RatingTriggerType.MODERATOR;
            case "SCHEDULED" -> RatingTriggerType.SCHEDULED;

            default -> throw new NotCorrectInput("Unexpected value: " + s.toUpperCase());
        };
    }
}
