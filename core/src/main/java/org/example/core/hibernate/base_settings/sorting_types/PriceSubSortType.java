package org.example.core.hibernate.base_settings.sorting_types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PriceSubSortType {
    ASC(1), DESC(2),
    CREATED_AT_ASC(3), CREATED_AT_DESC(4),
    GOOD_ID_ASC(5), GOOD_ID_DESC(6),
    PRICE_ASC(7), PRICE_DESC(8),
    USER_ID_ASC(9), USER_ID_DESC(10);

    private int value;
    private PriceSubSortType(int value) {
        this.value = value;
    }

    @JsonValue
    public String getValue(){
        return String.valueOf(value);
    }

    @JsonCreator
    public static PriceSubSortType forValue(int value) {
        for (PriceSubSortType type : PriceSubSortType.values()) {
            if (type.value == value) {
                return type;
            }
        }

        throw new IllegalArgumentException(" PriceSubSortType unknown code: " + value);    }
}
