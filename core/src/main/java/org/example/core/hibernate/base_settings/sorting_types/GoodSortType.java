package org.example.core.hibernate.base_settings.sorting_types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum GoodSortType {
    ASC(1), DESC(2),
    NAME_ASC(3), NAME_DESC(4),
    CAT_ASC(5), CAT_DESC(6),
    RATE_ASC(7),RATE_DESC(8),

    UPDATED_AT_ASC(9), UPDATED_AT_DESC(10),
    CREATED_AT_ASC(11), CREATED_AT_DESC(12);

    private int value;
    private GoodSortType(int value) {
        this.value = value;
    }
    @JsonValue
    public int getValue() {
        return value;
    }

    @JsonCreator
    public static GoodSortType forValue(int value) {
        for (GoodSortType type : GoodSortType.values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("GoodSortType Unknown sort type " + value);
    }


}
