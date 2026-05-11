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
    public static GoodStatusFromModerator fromJson(Object raw) {
        if (raw instanceof Integer) {
            int code = (Integer) raw;
            for (GoodStatusFromModerator type : values()) {
                if (type.value == code) return type;
            }
            throw new IllegalArgumentException("GoodStatusFromModerator  Unknown code: " + code);
        }
        if (raw instanceof String) {
            return GoodStatusFromModerator.valueOf((String) raw);
        }
        throw new IllegalArgumentException("GoodStatusFromModerator cannot deserialize: " + raw);
    }

}
