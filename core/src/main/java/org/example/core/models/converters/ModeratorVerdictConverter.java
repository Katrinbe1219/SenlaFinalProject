package org.example.core.models.converters;

import jakarta.persistence.AttributeConverter;
import org.example.core.models.types.ModeratorVerdict;

public class ModeratorVerdictConverter implements AttributeConverter<ModeratorVerdict, String> {
    @Override
    public String convertToDatabaseColumn(ModeratorVerdict moderatorVerdict) {
        return switch (moderatorVerdict){
            case APPROVED -> "APPROVED";
            case SUSPICIOUS -> "SUSPICIOUS";
            case RECALCULATED -> "RECALCULATED";
        };
    }

    @Override
    public ModeratorVerdict convertToEntityAttribute(String s) {
        return switch (s.toUpperCase()){
            case "APPROVED" -> ModeratorVerdict.APPROVED;
            case "SUSPICIOUS" -> ModeratorVerdict.SUSPICIOUS;
            case "RECALCULATED" -> ModeratorVerdict.RECALCULATED;
            default -> throw new IllegalArgumentException(" ModeratorVerdict Unknown value " + s);
        };
    }
}
