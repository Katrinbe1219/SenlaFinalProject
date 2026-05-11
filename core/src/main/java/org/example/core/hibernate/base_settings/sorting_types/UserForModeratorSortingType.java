package org.example.core.hibernate.base_settings.sorting_types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum UserForModeratorSortingType {
    ASC(1),
    DESC(2) ,
    LOGIN_ASC(3),
    LOGIN_DESC(4),
    USERNAME_ASC(5),
    USERNAME_DESC(6),
    UPDATED_AT_ASC(7),
    UPDATED_AT_DESC(8),
    CREATED_AT_ASC(9),
    CREATED_AT_DESC(10);

    private final int code;
    private UserForModeratorSortingType(int code) {
        this.code = code;
    }

    @JsonValue
    public int getValue(){
        return code;
    }

    @JsonCreator
    public static UserForModeratorSortingType fromCode(int code){
        for (UserForModeratorSortingType type : values()){
            if (type.code == code){
                return type;
            }
        }

        throw new IllegalArgumentException(" UserForModeratorSortingType unknown code: " + code);
    }
}
