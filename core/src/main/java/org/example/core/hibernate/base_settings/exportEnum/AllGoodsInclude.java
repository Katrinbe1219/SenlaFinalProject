package org.example.core.hibernate.base_settings.exportEnum;

import org.example.core.exceptions.NotCorrectInput;

public enum AllGoodsInclude {
    UNITS, CATEGORIES, TAGS;

    public static AllGoodsInclude getEnum(String value) {
        try{
            return valueOf(value.toUpperCase());
        }
        catch (IllegalArgumentException ex) {
            throw new NotCorrectInput(" AllGoodsInclude Unknown include value: " + value);
        }
    }
}
