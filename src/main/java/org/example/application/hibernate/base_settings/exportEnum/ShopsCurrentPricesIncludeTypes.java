package org.example.application.hibernate.base_settings.exportEnum;

import org.example.application.exceptions.NotCorrectInput;

public enum ShopsCurrentPricesIncludeTypes {
    TAGS, SHOPS, CATEGORIES;

    public static ShopsCurrentPricesIncludeTypes fromString(String value) {
        try{
            return valueOf(value.toUpperCase());
        }
        catch (IllegalArgumentException ex) {
            throw new NotCorrectInput("Unknown include value: " + value);
        }
    }
}
