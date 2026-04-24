package org.example.core.models.types;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum GoodStatusFromModerator {
    SUSPICIOUS, APPROVED;


    @JsonValue
    public String getValue(){
        return this.name();
    }

    @JsonCreator
    public static GoodStatusFromModerator fromValue(String value) {
        for (GoodStatusFromModerator type : values()) {
            if (type.name().equals( value.toUpperCase())) {
                return type;
            }
        }
        throw new IllegalArgumentException(" ReviewSortTypes Unknown code: " + value);
    }

}
