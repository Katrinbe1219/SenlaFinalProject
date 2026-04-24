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
    public static RatingStatus fromCode(int code) {
        for (RatingStatus type : values()) {
            if (type.value == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }
}
