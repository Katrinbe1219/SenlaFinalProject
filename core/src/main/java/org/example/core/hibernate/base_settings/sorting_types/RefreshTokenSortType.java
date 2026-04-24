package org.example.core.hibernate.base_settings.sorting_types;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum RefreshTokenSortType {
    ASC(1), DESC(2), EXPIRES_AT_ASC(3), EXPIRES_AT_DESC(4),
    CREATED_AT_ASC(5), CREATED_AT_DESC(6), LAST_USED_AT_ASC(7),
    LAST_USED_AT_DESC(8);

    private final int code;

    private RefreshTokenSortType(int code) {
        this.code = code;
    }

    @JsonCreator
    public static RefreshTokenSortType forValue(int value) {
        for (RefreshTokenSortType type : RefreshTokenSortType.values()) {
            if (type.code == value) {
                return type;
            }
        }
        throw new IllegalArgumentException(" RefreshTokenSortType unknown code: " + value);
    }
}
