package org.example.core.models.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RatingStatus {
   FIRST_ADDED(1),
     SUCCESS(2),
    FAILED(3);
    private int value;
    RatingStatus(int value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return this.name();
    }


    @JsonCreator
    public static RatingStatus fromJson(Object raw) {
        if (raw instanceof Integer) {
            int code = (Integer) raw;
            for (RatingStatus type : values()) {
                if (type.value == code) return type;
            }
            throw new IllegalArgumentException("Unknown code: " + code);
        }
        if (raw instanceof String) {
            return RatingStatus.valueOf((String) raw);
        }
        throw new IllegalArgumentException("Cannot deserialize: " + raw);
    }
}
