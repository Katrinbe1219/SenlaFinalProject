package org.example.application.models.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.example.application.exceptions.NotCorrectInput;
import org.example.application.models.types.RoleTypes;

@Converter(autoApply = true)
public class RoleTypeConverter implements AttributeConverter<RoleTypes, String> {

    @Override
    public String convertToDatabaseColumn(RoleTypes roleTypes) {
        return switch (roleTypes){
            case ADMIN -> "ADMIN";
            case ANALYST -> "ANALYST";
            case MODERATOR -> "MODERATOR";
            case MIN_USER -> "MIN_USER";
            case MAX_USER -> "MAX_USER";
        };
    }

    @Override
    public RoleTypes convertToEntityAttribute(String s) {
        return switch (s.toUpperCase()){
            case "ADMIN" -> RoleTypes.ADMIN;
            case "ANALYST" -> RoleTypes.ANALYST;
            case "MODERATOR" -> RoleTypes.MODERATOR;
            case "MIN_USER" -> RoleTypes.MIN_USER;
            case "MAX_USER" -> RoleTypes.MAX_USER;

            default -> throw new NotCorrectInput("Unexpected value: " + s.toUpperCase());
        };
    }
}
