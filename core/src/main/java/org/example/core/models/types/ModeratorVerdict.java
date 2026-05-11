package org.example.core.models.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ModeratorVerdict {
    APPROVED(1), SUSPICIOUS(2), RECALCULATED(3);

    private final int value;

    ModeratorVerdict(int value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return this.name();
    }

    private int getCode(){return this.value;}


    @JsonCreator
    public static ModeratorVerdict fromJson(Object raw) {
        if (raw instanceof Integer) {
            int code = (Integer) raw;
            for (ModeratorVerdict type : values()) {
                if (type.value == code) return type;
            }
            throw new IllegalArgumentException("Unknown code: " + code);
        }
        if (raw instanceof String) {
            return ModeratorVerdict.valueOf((String) raw);
        }
        throw new IllegalArgumentException("Cannot deserialize: " + raw);
    }



}
