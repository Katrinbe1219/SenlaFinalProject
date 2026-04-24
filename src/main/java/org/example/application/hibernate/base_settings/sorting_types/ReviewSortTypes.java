package org.example.application.hibernate.base_settings.sorting_types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.example.application.models.types.RatingTriggerType;

public enum ReviewSortTypes {
    ASC(1), DESC(2),
    GOOD_ASC(3), GOOD_DESC(4),
    USER_ID_ASC(5), USER_ID_DESC(6),
    RATE_ASC(7), RATE_DESC(8),
    CREATED_AT_ASC(9), CREATED_AT_DESC(10);

    private final int code;
    ReviewSortTypes(int value) {
        code = value;

    }

    @JsonValue
    public String getValue() {
        return this.name();
    }

    @JsonCreator
    public static ReviewSortTypes fromCode(int code) {
        for (ReviewSortTypes type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException(" ReviewSortTypes Unknown code: " + code);
    }

}
