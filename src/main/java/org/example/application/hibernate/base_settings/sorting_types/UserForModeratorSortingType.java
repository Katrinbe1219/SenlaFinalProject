package org.example.application.hibernate.base_settings.sorting_types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum UserForModeratorSortingType {
    ASC(1),
    DESC(2) ,
    LOGIN_ASC(3),
    LOGIN_DESC(4),
    USERNAME_ASC(5),
    USERNAME_DESC(6);

    private final int code;
    private UserForModeratorSortingType(int code) {
        this.code = code;
    }

    @JsonValue
    public String getValue(){
        return name();
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
