package org.example.application.models.converters;

import jakarta.persistence.AttributeConverter;
import org.example.application.models.types.GoodStatusFromModerator;

public class GoodStatusFromModeratorConverter implements AttributeConverter<GoodStatusFromModerator, String> {
    @Override
    public String convertToDatabaseColumn(GoodStatusFromModerator goodStatusFromModerator) {
        return switch (goodStatusFromModerator){
            case SUSPICIOUS -> "SUSPICIOUS";
            case APPROVED -> "APPROVED";
        };
    }

    @Override
    public GoodStatusFromModerator convertToEntityAttribute(String s) {
        return switch (s.toUpperCase()){
            case "APPROVED" -> GoodStatusFromModerator.APPROVED;
            case "SUSPICIOUS" -> GoodStatusFromModerator.SUSPICIOUS;
            default -> throw new IllegalArgumentException(" GoodStatusFromModerator Unknown value " + s);
        };
    }
}
