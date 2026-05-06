package org.example.core.models.types;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum GoodStatusFromModerator {
    SUSPICIOUS(1), APPROVED(2);
    private final int value;
    private GoodStatusFromModerator(int value) {
        this.value = value;
    }


    @JsonValue
    public String getValue(){
        return this.name();
    }

    @JsonCreator
    public static GoodStatusFromModerator fromValue(int value) {
        for (GoodStatusFromModerator type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException(" ReviewSortTypes Unknown code: " + value);
    }

}
