package org.example.application.models.types;

import com.fasterxml.jackson.annotation.JsonCreator;
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
    public static ModeratorVerdict forValue(int value) {
        for (ModeratorVerdict item : ModeratorVerdict.values()) {
            if (item.getCode() == value) {
                return item;
            }
        }
        throw new IllegalStateException("ModeratorVerdict Unknown code " + value);
    }


}
