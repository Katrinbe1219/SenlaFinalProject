package org.example.core.hibernate.base_settings.sorting_types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AvailabilitySubSortType {
    ASC(1), DESC(2),
    CREATED_AT_ASC(3), CREATED_AT_DESC(4),
    GOOD_ID_ASC(5), GOOD_ID_DESC(6),
    SHOP_ID_ASC(7), SHOP_ID_DESC(8),
    USER_ID_ASC(9), USER_ID_DESC(10);

    private int code;
    private AvailabilitySubSortType(int code) {
        this.code = code;
    }

    @JsonValue
    public int getValue(){
        return code;
    }


    @JsonCreator
    public static AvailabilitySubSortType forValue(int code){
        for (AvailabilitySubSortType filter : AvailabilitySubSortType.values()) {
            if (filter.code == code){
                return filter;
            }
        }
        throw new IllegalArgumentException(" AvailabilitySubFilter unknown code: " + code);    }

}
