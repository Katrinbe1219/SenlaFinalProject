package org.example.application.hibernate.base_settings.sorting_types;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PriceSortTypes {
    ASC(1),
    DESC(2),
    DATE_ASC(3),
    DATE_DESC(4),
    PRICE_ASC(5),
    PRICE_DESC(5);

    private final int code;
    PriceSortTypes(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    @JsonValue
    public String getValue() {
        return this.name();
    }
}
