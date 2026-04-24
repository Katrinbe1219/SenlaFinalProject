package org.example.application.hibernate.base_settings.exportEnum;

import org.example.application.exceptions.NotCorrectInput;

public enum AllGoodsInclude {
    UNITS, CATEGORIES, TAGS;

    public static AllGoodsInclude getEnum(String value) {
        try{
            return valueOf(value.toUpperCase());
        }
        catch (IllegalArgumentException ex) {
            throw new NotCorrectInput("Unknown include value: " + value);
        }
    }
}
