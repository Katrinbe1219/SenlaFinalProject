package org.example.core.hibernate.base_settings.sorting_types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.example.core.models.types.GoodStatusFromModerator;

public enum ModeratorRecalcSortType {
    ASC(1), DESC(2), GOOD_ID_ASC(3),
    GOOD_ID_DESC(4), MODERATOR_ID_ASC(5), MODERATOR_ID_DESC(6),
    DATE_ASC(7), DATE_DESC(8);

    private final int value;
    private ModeratorRecalcSortType(int value) {
        this.value = value;
    }

    @JsonValue
    public String getValue(){
        return this.name();
    }

    @JsonCreator
    public static ModeratorRecalcSortType fromJson(Object raw) {
        if (raw instanceof Integer) {
            int code = (Integer) raw;
            for (ModeratorRecalcSortType type : values()) {
                if (type.value == code) return type;
            }
            throw new IllegalArgumentException(" ModeratorRecalcSortType unknown code: " + code);
        }
        if (raw instanceof String) {
            return ModeratorRecalcSortType.valueOf((String) raw);
        }
        throw new IllegalArgumentException(" ModeratorRecalcSortType cannot deserialize: " + raw);
    }


}


